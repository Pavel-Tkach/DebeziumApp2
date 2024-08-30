package org.example.debeziumapp2.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.debeziumapp2.dto.ChangeDto;
import org.example.debeziumapp2.entity.Change;
import org.example.debeziumapp2.entity.Package;
import org.example.debeziumapp2.entity.enums.ChangeStatus;
import org.example.debeziumapp2.entity.enums.PackageStatus;
import org.example.debeziumapp2.exception.ChangeExecutedException;
import org.example.debeziumapp2.exception.ChangeNotFoundException;
import org.example.debeziumapp2.exception.PackageNotFoundException;
import org.example.debeziumapp2.exception.ViolationArchiveIntegrityException;
import org.example.debeziumapp2.mapper.ChangeMapper;
import org.example.debeziumapp2.repository.api.ChangeRepository;
import org.example.debeziumapp2.repository.api.PackageRepository;
import org.example.debeziumapp2.service.api.ChangeService;
import org.example.debeziumapp2.util.JsonParser;
import org.example.debeziumapp2.util.SqlGenerator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangeServiceImpl implements ChangeService {

    private final ChangeRepository changeRepository;

    private final PackageRepository packageRepository;

    private final ChangeMapper mapper;

    private final SqlGenerator sqlGenerator;

    private final JsonParser jsonParser;

    @Override
    public List<ChangeDto> findAll(String tableName) {
        List<Change> changeByName = changeRepository.findAllByTableName(tableName);

        return changeByName.stream()
                .map(mapper::toDto)
                .toList();
    }

    @SneakyThrows
    @Transactional
    @Override
    public void loadPackage(MultipartFile file) {
        Package pack = extractZip(file.getInputStream().readAllBytes());
        pack.setStatus(PackageStatus.LOADED);
        packageRepository.save(pack);
    }

    @Transactional
    @Override
    public void executePackage(Long packageId) {
        Package pack = packageRepository.findById(packageId)
                .orElseThrow(() -> new PackageNotFoundException("Package not found"));
        RestTemplate restTemplate = new RestTemplate();
        pack.getChanges().forEach(change -> {
            if (isChangeExecuted(change)) {
                throw new ChangeExecutedException("Change is already executed");
            }
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            String studentChangeSql = change.getSql();
            params.add("sql", studentChangeSql);
            restTemplate.postForLocation("http://localhost:11113/studentChange/apply", params);
            change.setStatus(ChangeStatus.EXECUTED);
        });
    }

    @Transactional
    @Override
    public void executeChange(Long changeId) {
        Change change = changeRepository.findById(changeId)
                .orElseThrow(() -> new ChangeNotFoundException("Change not found"));
        if (isChangeExecuted(change)) {
            throw new ChangeExecutedException("Change is already executed");
        }
        String studentChangeSql = change.getSql();
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("sql", studentChangeSql);
        restTemplate.postForLocation("http://localhost:11113/studentChange/apply", params);
        change.setStatus(ChangeStatus.EXECUTED);
    }

    public Package extractZip(byte[] zipData) {
        ObjectMapper mapper = new ObjectMapper();
        Package pack = null;
        String jsonPackage = "";
        try (ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(zipData);
             ZipInputStream zipInputStream = new ZipInputStream(arrayInputStream)) {
            ZipEntry entry = zipInputStream.getNextEntry();
            if (entry != null && "package.txt".equals(entry.getName())) {
                StringBuilder builder = new StringBuilder();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = zipInputStream.read(buffer)) != -1) {
                    builder.append(new String(buffer, 0, length));
                }
                jsonPackage = builder.toString();
                pack = mapper.readValue(jsonPackage, Package.class);
            }
            ZipEntry entry2 = zipInputStream.getNextEntry();
            if (entry2 != null && "secret.txt".equals(entry2.getName())) {
                StringBuilder secretBuilder = new StringBuilder();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = zipInputStream.read(buffer)) != -1) {
                    secretBuilder.append(new String(buffer, 0, length));
                }
                String secretHash = secretBuilder.toString();
                if (!secretHash.equals(generateOriginalHash(jsonPackage))) {
                    throw new ViolationArchiveIntegrityException("Secret hash does not match");
                }
            }
        } catch (IOException ex) {
            log.error("Ошибка при извлечении данных: {}", ex.getMessage());
            throw new RuntimeException("Ошибка при извлечении данных: " + ex.getMessage());
        }

        return pack;
    }

    private String generateOriginalHash(String jsonPackage) {
        return DigestUtils.md5DigestAsHex(jsonPackage.getBytes()).toUpperCase();
    }

    private boolean isChangeExecuted(Change change) {
        String[] beforeAndAfter = jsonParser.getBeforeAndAfter(change.getChangeContent());
        Map<String, String> fieldAndValue = jsonParser.parseAfterData(beforeAndAfter[2]);
        String selectSql = sqlGenerator.generateSelectSql(change.getTableName(), fieldAndValue);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("SQL-Query", selectSql);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        Integer amountExistedNotes = restTemplate.exchange("http://localhost:11113/studentChange", HttpMethod.GET, entity, Integer.class).getBody();
        if ("null".equals(beforeAndAfter[1])) {
            return amountExistedNotes == 0 ? false : true;
        } else {
            return amountExistedNotes == 0 ? true : false;
        }
    }
}
