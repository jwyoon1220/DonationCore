package io.github.jwyoon1220.donationCore.addon.api

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Addon(
    val name: String,
    val description: String
) // Addon 어노테이션은 Addon 클래스를 식별하기 위한 메타데이터를 제공합니다. name과 description 속성을 통해 Addon의 이름과 설명을 지정할 수 있습니다. (디버그용, 실제 이름은 addon.yml에서 가져옴)