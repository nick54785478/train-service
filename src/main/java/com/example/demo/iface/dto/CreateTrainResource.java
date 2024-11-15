package com.example.demo.iface.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "火車車次資訊")
public class CreateTrainResource {

	@Schema(description = "停靠順序")
	private Integer seq;

	@Schema(description = "火車代號 uuid")
	private Integer trainNo; // 火車代號

	@Schema(description = "火車種類")
	private String trainKind;

	@Schema(description = "停靠站清單")
	private List<CreateStopResource> stops;
}
