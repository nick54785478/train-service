package com.example.demo.base.config;

import java.util.HashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfiguration {

	@Bean
	public OpenAPI springShopOpenAPI() {
		return new OpenAPI()
				// 表示API的基本訊息，包括標題、版本號、描述等。
				.info(new Info().title("My Train Demo API ") // 標題
						.description("我的 API 文檔") // 敘述
						.version("v1") // 版本號

						// 授權許可資訊(license)，用於描述API的授權訊息，包括姓名、URL等；假設目前的授權資訊為 Apache 2.0 的開源標準
						.license(new License().name("Apache 2.0") // License 名稱
								.url("http://springdoc.org").identifier("Apache-2.0") // 標示授權許可
								.extensions(new HashMap<>()) // 使用Map配置信息（如key为"name","url","identifier"）
						)) // 授權訊息

				// 建立外部文檔
				.externalDocs(new ExternalDocumentation().description("外部文檔") // 敘述
						.url("https://github.com/nick54785478/train-demo.git") // 連接url
				);
	}

}
