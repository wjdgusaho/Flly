package com.ssafy.flowerly.flly.service;

import com.ssafy.flowerly.entity.Flower;
import com.ssafy.flowerly.entity.type.SituationType;
import com.ssafy.flowerly.flly.dto.FlowerDto;
import com.ssafy.flowerly.flly.dto.FlowerRequestDto;
import com.ssafy.flowerly.flly.repository.FlowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlowerService {
    private final FlowerRepository flowerRepository;
    public List<FlowerDto> getFlowerList(FlowerRequestDto flowerRequest) {
//        System.out.println(flowerRequest.getSituation());
//        System.out.println(SituationType.사랑);
//        System.out.println(flowerRequest.getSituation().equals(SituationType.사랑));
        List<Flower> flowerList = flowerRepository.findFlowersByColorAndRecommendation(
                flowerRequest.getColors(), flowerRequest.getSituation(), flowerRequest.getTarget());

        List<FlowerDto> flowerDtoList = new ArrayList<>();
        for(Flower flower: flowerList) {
            flowerDtoList.add(FlowerDto.of(flower));
        }

        return flowerDtoList;
    }
}
