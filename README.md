# DonationCore

DonationCore는 Minecraft 서버용 Spigot/Bukkit 플러그인으로, 스트리머 도네이션 연동과 커스텀 애드온 기능을 지원합니다. 서버 운영자는 이를 통해 실시간 도네이션 이벤트를 처리하고, 플러그인을 확장하여 맞춤형 기능을 추가할 수 있습니다.

## 주요 기능

1. **스트리머 도네이션 연동**
- 실시간 도네이션 알림 처리
- 다양한 플랫폼 지원 (후원, 구독, 팁 등)
- 글로벌 리스너 시스템을 통해 애드온이나 다른 플러그인에서 도네이션 이벤트 처리 가능

2. **애드온 시스템**
- 기능 확장을 위한 애드온 로딩 지원
- 애드온은 JAR 파일 형태로 제공
- 각 애드온은 자체 설정 파일(config.yml)과 메인 클래스(main) 정의 가능
- 런타임 중 애드온 로드 및 언로드 가능

3. **커맨드 지원**
- 기본 도네이션 연결 및 테스트용 명령어 제공
- 서버 운영자가 도네이션 연동 상태 확인 가능

## 설치 방법
1. 서버의 plugins 폴더에 DonationCore JAR 파일을 넣습니다.
2. 서버를 시작하면 기본 설정 파일과 addons 폴더가 생성됩니다.
3. 필요한 애드온을 addons 폴더에 추가하고 서버를 재시작하거나 런타임 로드 기능을 사용합니다.

## 설정
- `config.yml`: DonationCore 기본 설정
- 각 애드온 폴더/config.yml: 애드온별 설정

## 확장
- StreamListener 인터페이스를 구현하여 글로벌 도네이션 이벤트 처리 가능
- AddonManager를 통해 애드온 관리 및 클래스 로더 접근 가능

## 지원 환경
- Spigot/Bukkit/Paper(1.21.5 미만) 기반 Minecraft 서버
- Java 17 이상 권장

## 개발자 가이드 / Developer Guide
- DonationCore는 손쉽게 애드온을 개발할 수 있도록 설계되었습니다.
- 애드온 개발을 위해서는 Java 언어와 Spigot API에 대한 기본적인 이해가 필요합니다

```kotlin
package com.example.myaddon

import io.github.jwyoon1220.donationCore.addon.api.DonationCoreAddon
import io.github.jwyoon1220.donationCore.DonationCore
import io.github.jwyoon1220.donationCore.stream.Streamer
import io.github.jwyoon1220.donationCore.stream.Platform
import io.github.jwyoon1220.donationCore.stream.DonationType
import io.github.jwyoon1220.donationCore.addon.api.StreamListener

// name은 꼭 있어야 합니다. 안그러면 NoSuchMethodException뜸
class MyAddon(name: String) : DonationCoreAddon(name) {

    private val donationListener = object : StreamListener {
        override fun onDonation(
            streamer: Streamer,
            platform: Platform, // CHZZK or SOOP
            type: DonationType, // NORMAL, MISSON 오타나서 저렇게 써야됨.
            profile: Streamer.Donation // payAmount, name, message 등 도네이션 정보 담긴 객체 (왜 profile이냐고요? 저도 몰라요 귀찮아서 안바꿈)
        ) {
            // 도네이션 이벤트 처리 예시
            logger.info("Received ${type.name} from ${profile.name} on ${platform.name}")
        }
    }

    override fun onEnable() {
        // 애드온 활성화 시 실행
        logger.info("MyAddon has been enabled!")

        // 글로벌 리스너 등록
        // manager는 수퍼 클래스에서 제공됩니다.
        manager.addGlobalListener(donationListener)
        
        // 전역 리스너와 동일하게 각각의 플레이어마다 리스너를 등록할 수도 있습니다.
        // Streamer는 도네이션이 들어오는 스트리머를 나타내는 객체입니다. GlobalEventListener.streamers에서 현재 등록된 모든 스트리머를 가져와서 각각에 리스너를 추가할 수 있습니다.
        for ((name, streamer) in GlobalEventListener.streamers) {
            streamer.addListener(donationListener)
        }
    }

    override fun onDisable() {
        // 애드온 비활성화 시 실행
        logger.info("MyAddon has been disabled!")

        // 글로벌 리스너 제거
        // 뭐... 굳이 안하셔도 됩니다. 어짜피 님들 Listner도 안하잖아요? JVM이 알아서 할?겁니다.
        DonationCore.manager.removeGlobalListener(donationListener)
    }
}
```