package com.example.demo.iface.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainResource {

	private Integer seq;

	private Integer trainNo; // 火車代號

	private String trainKind;

	private List<CreateStopResource> stops;
}
