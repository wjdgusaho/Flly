package com.ssafy.flowerly.seller.model;


import com.ssafy.flowerly.address.repository.DongRepository;
import com.ssafy.flowerly.address.repository.SigunguRepository;
import com.ssafy.flowerly.entity.*;
import com.ssafy.flowerly.entity.type.ProgressType;
import com.ssafy.flowerly.entity.type.UploadType;
import com.ssafy.flowerly.exception.CustomException;
import com.ssafy.flowerly.exception.ErrorCode;
import com.ssafy.flowerly.member.MemberRole;
import com.ssafy.flowerly.member.model.MemberRepository;
import com.ssafy.flowerly.member.model.StoreInfoRepository;
import com.ssafy.flowerly.s3.model.S3Service;
import com.ssafy.flowerly.seller.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerService {

    private final FllyRepository fellyRepository;
    private final RequestRepository requestRepository;
    private final FllyParticipationRepository fllyParticipationRepository;
    private final MemberRepository memberRepository;
    private final StoreDeliveryRegionRepository storeDeliveryRegionRepository;
    private final FllyDeliveryRegionRepository fllyDeliveryRegionRepository;
    private final StoreInfoRepository storeInfoRepository;
    private final DongRepository dongRepository;
    private final SigunguRepository sigunguRepository;
    private final FllyPickupRegionRepository fllyPickupRegionRepository;
    private final S3Service s3Service;


    /*
        의뢰 내용 API
     */
    public FllyRequestDto getRequestLetter(long fllyId) {
        return null;
    }

    /*
        채택된 주문 리스트
     */

    public Page<OrderSelectSimpleDto> getOrderSelect(Long mamberId, Pageable pageable) {
        //내꺼인지
        //주문완료인 제작완료인지
        Page<OrderSelectSimpleDto> oderBySelect =
                requestRepository.findBySellerMemberIdOrderByCreatedAt(mamberId, pageable)
                        .map(Request::toOrderSelectSimpleDto);
        return oderBySelect;
    }

    /*
        채택된 주문 완료하기
     */
    @Transactional
    public String UpdateProgressType(Long mamberId, Long fllyId) {

        //내가 참여한건지 (주문서인지)

       //주문오나료랑 제작완료 인애만 떠야함 

        Flly fllyInfo = fellyRepository.findByFllyId(fllyId).orElseThrow();
        if(fllyInfo.getProgress().getTitle().equals("주문완료")){
            fllyInfo.UpdateFllyProgress(ProgressType.FINISH_MAKING);
        }
        if(fllyInfo.getProgress().getTitle().equals("제작완료")) {
            fllyInfo.UpdateFllyProgress(ProgressType.FINISH_DELIVERY);
        }
        Flly updateInfo = fellyRepository.save(fllyInfo);

        return updateInfo.getProgress().getTitle();
    }

    /*
        참여한 플리
     */
    public Page<OrderParticipationDto> getParticipation(Long memberId ,Pageable pageable){
        //입찰인지 조율이지 + 경매마감시간이 안지난것만 보여져야한다
        LocalDateTime currentDateTime = LocalDateTime.now();
        log.info(currentDateTime.toString());
        Page<OrderParticipationDto> orderParticipation =
                fllyParticipationRepository.findBySellerMemberIdParticipationDto(memberId, pageable, currentDateTime)
                        .map(FllyParticipation::toOrderParticipationDto);

        return orderParticipation;
    }

    /*
       플리 의뢰서 상세 ( 제안 + 의뢰 )
     */
    public ParticipationRequestDto getFllyRequestInfo(Long memberId, Long fllyId){

//        ParticipationRequestDto result = new ParticipationRequestDto();
//        //FllyRequestDto fllyRequestDto = getRequestLetter(fllyId);
//        result.setFllyRequestDto(fllyRequestDto);
//        FllyResponeDto fllyResponeDto = fllyParticipationRepository.findByFllyFllyId(fllyId)
//                .map(FllyParticipation::toFllyResponeDto).orElseThrow();
//        result.setFllyResponeDto(fllyResponeDto);

        return null;
    }

    /*
        플리 참여하기
     */
    @Transactional
    public void sellerFllyParticipate(Long memberId, MultipartFile file, RequestFllyParticipateDto data) {

        //flly가 있는으면서 활성화가 되어있는가 ?
        Flly fllyInfo = fellyRepository.findByFllyIdAndActivate(data.getFllyId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FIND_FLLY));
        //유저가 있는가 ?
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FIND_MEMBER));
        //유저가 판매자가 아니라면?
        if(member.getRole() != MemberRole.SELLER){
            throw new CustomException(ErrorCode.MEMBER_NOT_SELLER);
        }

        //이미 해당 유저가 참여한 플리라면(이미 참가하신 플리입니다)
        fllyParticipationRepository
                .findByFllyFllyIdAndSellerMemberId(memberId, data.getFllyId())
                .ifPresent(fllyParticipation -> {
                    throw new CustomException(ErrorCode.SELLER_ALREADY_PARTICIPATE);
                });

        String imgUrl = s3Service.uploadOneImage(file, UploadType.ORDER);

        if(imgUrl.isEmpty()){
            throw new CustomException(ErrorCode.INVALID_UPLOAD_FILE);
        }

        try {
            fllyParticipationRepository
                    .save(FllyParticipation.builder()
                            .flly(fllyInfo)
                            .seller(member)
                            .imageUrl(imgUrl)
                            .offerPrice(data.getOfferPrice())
                            .content(data.getContent())
                            .build());
        }catch (Exception e){
            throw new CustomException(ErrorCode.SELLER_PARTICIPATE_FAIL);
        }

    }
    
    /*
        주변 플리 정보 불러오기
     */
    public Page<FllyNearDto> getNearFllyDeliverylist(Long memberId, Pageable pageable) {

        //유저가 있는가 ? (판매자)
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FIND_MEMBER));
        //유저가 판매자가 아니라면?
        if(member.getRole() != MemberRole.SELLER){
            throw new CustomException(ErrorCode.MEMBER_NOT_SELLER);
        }

        //판매자가 배달 가능한 지역인가?
        //1. 판매자가 설정한 배달 정보를 가져와야한다.
        /* 이런 방법도 가능하다!! 바로 DTO 만들어서 하기..
        List<AddressSimpleDto> storeDeliveryRegion = storeDeliveryRegionRepository.findBySellerMemberId(memberId)
                .map(regions -> regions.stream()
                        .map(StoreDeliveryRegion::toAddressSimpleDto)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_SELLER_DELIVERY_REGION));
         */

        List<StoreDeliveryRegion> storeDeliveryRegions = storeDeliveryRegionRepository.findBySellerMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_SELLER_DELIVERY_REGION));

        //배달일경우는 전체를 한다면 (충청북도)로 찾아야하기에 세개다 비교를 해야함
        List<Sido> deliverySido = new ArrayList<>();
        List<Sigungu> deliverySigugun = new ArrayList<>();
        List<Dong> deliveryDong = new ArrayList<>();

        //시 구군 동 세팅 (동부터 돌면서 전체가 있나 없나 비교)
        for(StoreDeliveryRegion tmp : storeDeliveryRegions){
            if(tmp.getDong().getDongName().equals("전체")){
                if(tmp.getSigungu().getSigunguName().equals("전체")) deliverySido.add(tmp.getSido());
                else deliverySigugun.add(tmp.getSigungu());
            }
            else deliveryDong.add(tmp.getDong());
        }

        // 해당 배달지역으로 가지고있는 것을 Flly번호를 찾아온다.
        Page<FllyNearDto> deliveryAbleList = fllyDeliveryRegionRepository
                .getSellerDeliverAbleList(deliverySido, deliverySigugun, deliveryDong, pageable)
                .map(FllyDeliveryRegion::toDeliveryFllyNearDto);

        if(deliveryAbleList.getContent().size() <= 0){
            throw new CustomException(ErrorCode.NOT_SELLER_SEARCH_NEAR);
        }

        return deliveryAbleList;
    }

    /*
        주변 플리 정보 불러오기
     */
    public Page<FllyNearDto> getNearFllyPickuplist(Long memberId, Pageable pageable) {

        Page<FllyNearDto> pickupAbleList = null;
        //유저가 있는가 ? (판매자)
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FIND_MEMBER));
        //유저가 판매자가 아니라면?
        if(member.getRole() != MemberRole.SELLER){
            throw new CustomException(ErrorCode.MEMBER_NOT_SELLER);
        }

        //2 픽업 가능한지 찾아야한다!
        //2-1 판매자 가게의 주소
        //없다고 화면에 출력이 안되는게 아니기때문에 에러발생 X
        StoreInfo store = storeInfoRepository.findBySellerMemberId(memberId);

        //나의 주소를 가지고 전체 값을 찾아야한다! (시를 보내 구군의 전체를 찾고 / 시구군을 보내 동에서 전체를 찾는다 )
        if(store != null){
            Sigungu sigunguAll = sigunguRepository.findBysigunguCodeAllCode(store.getSido());
            Dong dongAll = dongRepository.findByDongCodeAllCode(store.getSigungu());

            //픽업일경우에는 전체 + 내 주소만 보면되기때문
            List<Sigungu> pickupSigugun = new ArrayList<>();
            List<Dong> pickupDong = new ArrayList<>();

            pickupSigugun.add(store.getSigungu());
            pickupSigugun.add(sigunguAll);
            pickupDong.add(store.getDong());
            pickupDong.add(dongAll);

            //2-2 가게의 시 군 구 와 전체 시군구 와 전체 동을 가지고 flly픽업정보에서 찾는다
            pickupAbleList =  fllyPickupRegionRepository
                    .getSellerPickupAbleList(pickupSigugun, pickupDong, pageable)
                    .map(FllyPickupRegion::toPickupFllyNearDto);

        }

        if(pickupAbleList == null || pickupAbleList.getContent().size() <= 0){
            throw new CustomException(ErrorCode.NOT_SELLER_SEARCH_NEAR);
        }
        return pickupAbleList;
    }
}