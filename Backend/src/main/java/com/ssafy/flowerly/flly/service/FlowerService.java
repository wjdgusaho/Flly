package com.ssafy.flowerly.flly.service;

import com.ssafy.flowerly.address.repository.DongRepository;
import com.ssafy.flowerly.address.repository.SidoRepository;
import com.ssafy.flowerly.address.repository.SigunguRepository;
import com.ssafy.flowerly.entity.*;
import com.ssafy.flowerly.entity.type.*;
import com.ssafy.flowerly.exception.CustomException;
import com.ssafy.flowerly.exception.ErrorCode;
import com.ssafy.flowerly.flly.dto.FllyDto;
import com.ssafy.flowerly.flly.dto.FlowerDto;
import com.ssafy.flowerly.flly.dto.FlowerRequestDto;
import com.ssafy.flowerly.flly.dto.MainDto;
import com.ssafy.flowerly.flly.repository.FlowerRepository;
import com.ssafy.flowerly.member.model.MemberRepository;
import com.ssafy.flowerly.s3.model.S3Service;
import com.ssafy.flowerly.seller.model.FllyDeliveryRegionRepository;
import com.ssafy.flowerly.seller.model.FllyPickupRegionRepository;
import com.ssafy.flowerly.seller.model.FllyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlowerService {
    private final FlowerRepository flowerRepository;
    private final FllyRepository fllyRepository;
    private final SidoRepository sidoRepository;
    private final SigunguRepository sigunguRepository;
    private final DongRepository dongRepository;
    private final FllyPickupRegionRepository fllyPickupRegionRepository;
    private final FllyDeliveryRegionRepository fllyDeliveryRegionRepository;
    private final S3Service s3Service;
    private final MemberRepository memberRepository;

    public Map<String, List<FlowerDto>> getFlowerList(FlowerRequestDto flowerRequest) {
        Map<String, List<FlowerDto>> lists = new HashMap<>();
        List<FlowerDto> flowerDtoList = new ArrayList<>();
        lists.put("flowers", null);
        lists.put("flowersColor", null);
        lists.put("flowersMeaning", null);

        if(flowerRequest.getSituation() == null && flowerRequest.getTarget() == null && flowerRequest.getColors() == null) {
            List<Flower> flowerList = new ArrayList<>();
            List<Long> indexList = new ArrayList<>();
            Random random = new Random();
            generateRandom(1, 7, indexList, random);
            generateRandom(15, 19, indexList, random);
            generateRandom(20, 24, indexList, random);
            generateRandom(25, 28, indexList, random);
            generateRandom(29, 31, indexList, random);
            generateRandom(32, 37, indexList, random);
            generateRandom(38, 43, indexList, random);
            generateRandom(44, 48, indexList, random);
            generateRandom(80, 85, indexList, random);
            generateRandom(75, 79, indexList, random);

            for(Long index: indexList) {
                flowerList.add(flowerRepository.findById(index).orElseThrow(() -> new CustomException(ErrorCode.FLOWER_NOT_FOUND)));
            }

            for(Flower flower: flowerList) {
                flowerDtoList.add(FlowerDto.of(flower));
            }

            lists.put("flowers", flowerDtoList);
            return lists;
        }

        List<SituationType> situationTypes = flowerRequest.getSituation();
        List<TargetType> targetTypes = flowerRequest.getTarget();
        List<ColorType> colorTypes = new ArrayList<>();
        for(String color: flowerRequest.getColors()) {
            colorTypes.add(ColorType.valueOf(color));
        }

        Pageable pageable = PageRequest.of(0, 20);
        if(flowerRequest.getSituation() == null) {
            situationTypes = new ArrayList<>();
            for(SituationType situationType: SituationType.values()) {
                situationTypes.add(situationType);
            }
        }
        if(flowerRequest.getTarget() == null) {
            targetTypes = new ArrayList<>();
            for(TargetType targetType: TargetType.values()) {
                targetTypes.add(targetType);
            }
        }
        if(flowerRequest.getColors() == null) {
            for(ColorType colorType: ColorType.values()) {
                colorTypes.add(colorType);
            }
        }
        List<Flower> flowerList = flowerRepository.findFlowersByRequest(
                colorTypes, situationTypes, targetTypes, pageable);

        for(Flower flower: flowerList) {
            flowerDtoList.add(FlowerDto.of(flower));
        }
        lists.put("flowers", flowerDtoList);

        if(flowerList.size() < 10) {
            List<FlowerDto> flowerDtoListColor = new ArrayList<>();
            if(flowerRequest.getColors() != null) {
                List<Flower> flowerListColor = flowerRepository.findFlowersByColor(colorTypes, pageable);

                for(Flower flower: flowerListColor) {
                    if(flowerDtoListColor.size() > 9) break;
                    FlowerDto flowerDto = new FlowerDto();
                    flowerDto.setFlowerCode(flower.getFlowerCode());
                    if(flowerDtoList.contains(flowerDto)) continue;
                    flowerDtoListColor.add(FlowerDto.of(flower));
                }
                lists.put("flowersColor", flowerDtoListColor);
            }

            List<Flower> flowerListMeaning = flowerRepository.findFlowersByMeaning(situationTypes, targetTypes, pageable);

            List<FlowerDto> flowerDtoListMeaning = new ArrayList<>();
            for(Flower flower: flowerListMeaning) {
                if(flowerDtoListMeaning.size() > 9) break;
                FlowerDto flowerDto = new FlowerDto();
                flowerDto.setFlowerCode(flower.getFlowerCode());
                if(flowerDtoList.contains(flowerDto) || flowerDtoListColor.contains(flowerDto)) continue;
                flowerDtoListMeaning.add(FlowerDto.of(flower));
            }
            lists.put("flowersMeaning", flowerDtoListMeaning);
        }

        return lists;
    }

    public static void generateRandom(int min, int max, List<Long> indexList, Random random) {
        indexList.add((long) (random.nextInt(max-min)+min));
        while(true) {
            Long temp = (long) (random.nextInt(max-min)+min);
            if(indexList.contains(temp)) continue;
            indexList.add(temp);
            break;
        }
    }

    public void saveFllyRequest(FllyDto fllyDto, Long memberId) {
        //=========================================================
        // String s3uploadUrl = s3Service.makeFlowerBouquet(이미지 URL);
        // 으로 쓰면 됩니다.
        //=========================================================

        Flly flly = new Flly();
        flly.setSituation(fllyDto.getSituation());
        flly.setTarget(fllyDto.getTarget());
        if(fllyDto.getColors().size() > 0) {
            flly.setColor1(ColorType.valueOf(fllyDto.getColors().get(0)));
            if(fllyDto.getColors().size() > 1) {
                flly.setColor2(ColorType.valueOf(fllyDto.getColors().get(1)));
                if(fllyDto.getColors().size() > 2) flly.setColor3(ColorType.valueOf(fllyDto.getColors().get(2)));
            }
        }
        if(fllyDto.getFlowers().size() > 0) {
            Flower flower1 = flowerRepository.findById(fllyDto.getFlowers().get(0).getFlowerCode())
                            .orElseThrow(() -> new CustomException(ErrorCode.FLOWER_NOT_FOUND));
            flly.setFlower1(flower1);
            if(fllyDto.getFlowers().size() > 1) {
                Flower flower2 = flowerRepository.findById(fllyDto.getFlowers().get(1).getFlowerCode())
                        .orElseThrow(() -> new CustomException(ErrorCode.FLOWER_NOT_FOUND));
                flly.setFlower2(flower2);
                if(fllyDto.getFlowers().size() > 2) {
                    Flower flower3 = flowerRepository.findById(fllyDto.getFlowers().get(2).getFlowerCode())
                            .orElseThrow(() -> new CustomException(ErrorCode.FLOWER_NOT_FOUND));
                    flly.setFlower3(flower3);
                }
            }
        }
        flly.setOrderType(fllyDto.getOrderType());
        flly.setDeadline(fllyDto.getDeadline().plusHours(9));
        flly.setRequestContent(fllyDto.getRequestContent());
        flly.setBudget(fllyDto.getBudget());
        flly.setImageUrl(s3Service.makeFlowerBouquet(fllyDto.getImageUrl()));
        flly.setCanceled(false);
        flly.setProgress(ProgressType.START);
        Member member = memberRepository.findByMemberId(memberId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        flly.setConsumer(member);

        Flly savedFlly = fllyRepository.save(flly);
        if(fllyDto.getOrderType().equals(OrderType.DELIVERY)) {
            Sido sido = sidoRepository.findBySidoName(fllyDto.getDelivery().getSido())
                            .orElseThrow(() -> new CustomException(ErrorCode.SIDO_NOT_FOUND));
            Sigungu sigungu = sigunguRepository.findBySigunguNameAndSido(fllyDto.getDelivery().getSigungu(), sido)
                    .orElseThrow(() -> new CustomException(ErrorCode.SIGUNGU_NOT_FOUND));

            Dong dong = dongRepository.findByDongNameAndSigungu(fllyDto.getDelivery().getDong(), sigungu)
                    .orElseThrow(() -> new CustomException((ErrorCode.DONG_NOT_FOUND)));

            FllyDeliveryRegion fllyDeliveryRegion = FllyDeliveryRegion.builder()
                    .flly(savedFlly)
                    .sido(sido)
                    .sigungu(sigungu)
                    .dong(dong)
                    .deliveryAddress(fllyDto.getDetailAddress())
                    .build();

            fllyDeliveryRegionRepository.save(fllyDeliveryRegion);
        } else {
            for (FllyDto.Pickup pickup : fllyDto.getPickup()) {
                Long sidoCode = pickup.getSidoCode();
                Long sigunguCode = pickup.getSigunguCode();
                Long dongCode = pickup.getDongCode();

                Sido pickupSido = sidoRepository.findBySidoCode(sidoCode)
                        .orElseThrow(() -> new CustomException(ErrorCode.SIDO_NOT_FOUND));

                Sigungu pickupSigungu = sigunguRepository.findBySigunguCodeAndSido(sigunguCode, pickupSido)
                        .orElseThrow(() -> new CustomException(ErrorCode.SIGUNGU_NOT_FOUND));

                Dong pickupDong = null;

                if(!pickupSigungu.getSigunguName().equals("전체")) {
                    pickupDong = dongRepository.findByDongCodeAndSigungu(dongCode, pickupSigungu)
                            .orElseThrow(() -> new CustomException(ErrorCode.DONG_NOT_FOUND));
                }

                FllyPickupRegion fllyPickupRegion = FllyPickupRegion.builder()
                        .flly(savedFlly)
                        .sido(pickupSido)
                        .sigungu(pickupSigungu)
                        .dong(pickupDong)
                        .build();

                fllyPickupRegionRepository.save(fllyPickupRegion);
            }
        }
    }

    public List<MainDto> getMainImages() {
        String month = LocalDate.now().getMonth().toString();
        List<Flower> flowerList = new ArrayList<>();
        switch (month) {
            case "JANUARY":
                flowerList = flowerRepository.findByJanuaryIsTrue();
                break;
            case "FEBRUARY":
                flowerList = flowerRepository.findByFebruaryIsTrue();
                break;
            case "MARCH":
                flowerList = flowerRepository.findByMarchIsTrue();
                break;
            case "APRIL":
                flowerList = flowerRepository.findByAprilIsTrue();
                break;
            case "MAY":
                flowerList = flowerRepository.findByMayIsTrue();
                break;
            case "JUNE":
                flowerList = flowerRepository.findByJuneIsTrue();
                break;
            case "JULY":
                flowerList = flowerRepository.findByJulyIsTrue();
                break;
            case "AUGUST":
                flowerList = flowerRepository.findByAugustIsTrue();
                break;
            case "SEPTEMBER":
                flowerList = flowerRepository.findBySeptemberIsTrue();
                break;
            case "OCTOBER":
                flowerList = flowerRepository.findByOctoberIsTrue();
                break;
            case "NOVEMBER":
                flowerList = flowerRepository.findByNovemberIsTrue();
                break;
            case "DECEMBER":
                flowerList = flowerRepository.findByDecemberIsTrue();
                break;
        }

        List<MainDto> mainDtoList = new ArrayList<>();
        HashMap<String, Long> startIdx = new HashMap<>();
        HashMap<String, Long> endIdx = new HashMap<>();
        Set<String> names = new HashSet<>();

        names.add(flowerList.get(0).getScientificName());
        startIdx.put(flowerList.get(0).getScientificName(), flowerList.get(0).getFlowerCode());
        for(int i=0; i<flowerList.size(); i++) {
            Flower curFlower = flowerList.get(i);
            if(!names.contains(curFlower.getScientificName())) {
                endIdx.put(flowerList.get(i-1).getScientificName(), flowerList.get(i-1).getFlowerCode());
                names.add(curFlower.getScientificName());
                startIdx.put(curFlower.getScientificName(),curFlower.getFlowerCode());
            }
        }
        endIdx.put(flowerList.get(flowerList.size()-1).getFlowerName(), flowerList.get(flowerList.size()-1).getFlowerCode());


        Set<String> selected = new HashSet<>();
        Random random = new Random();
        for(String name: names) {
            if(selected.contains(name)) continue;
            Flower flower = flowerRepository.findById(
                    Long.valueOf(random.nextInt((int)(endIdx.get(name)-startIdx.get(name))+1))+startIdx.get(name))
                    .orElseThrow(() -> new CustomException(ErrorCode.FLOWER_NOT_FOUND));
            mainDtoList.add(MainDto.of(flower));
            selected.add(name);
        }

//        Collections.sort(mainDtoList, new Comparator<MainDto>() {
//            @Override
//            public int compare(MainDto o1, MainDto o2) {
//                return o1.getFlowerCode() < o2.getFlowerCode()? -1 : 1;
//            }
//        });
        Collections.shuffle(mainDtoList);

        return mainDtoList;
    }
}
