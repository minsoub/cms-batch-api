# 콘텐츠 통합 관리 
- Batch

## 사용 기술

- JDK 17
- Kotlin
- Spring Boot Batch
- Test
    - junit5
    - mockk
    - kluent
    - kover
        - test coverage
- Datasource
    - MongoDB
    - Redis
        - redisson

##  프로젝트 구성

### 코딩 스타일

- Detekt(정적분석)
- Ktlint(스타일)

### 유의사항
M1의 경우 build.gradle.kts dependency 에 추가
(현재 추가 상태)
```
runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.77.Final:osx-aarch_64")
```

IDE Ktlint 설정
![img.png](img.png)
```
./gradlew addKtlintCheckGitPreCommitHook
```

detekt
```
./gradlew detekt
```

kover
```
./gradlew koverVerify
```