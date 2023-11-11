import React, { useState } from "react";
import style from "./style/MypageSetting.module.css";
import Image from "next/image";
import { useRouter } from "next/router";
import UserTypeChangeModal from "./MyPageSettingComponent/UserTypeChangeModal";
import { useRecoilState } from "recoil";
import { MemberInfo, memberInfoState } from "@/recoil/memberInfoRecoil";

const MypageSetting = () => {
  const [alarmState, setAlarmState] = useState<Boolean>(false);
  const [memberInfo, setMemberInfo] = useRecoilState<MemberInfo>(memberInfoState);
  const router = useRouter();

  //우저정보 변경 모달확인
  const [userChangModalState, setUserChangModalState] = useState<Boolean>(false);

  const userChangModalHandler = () => {
    setUserChangModalState(!userChangModalState);
  };

  return (
    <>
      <div className={style.SettingBack}>
        {userChangModalState && (
          <UserTypeChangeModal userChangModalHandler={userChangModalHandler} />
        )}
        <div className={style.SettingHeader}>
          <Image
            src="/img/btn/left-btn.png"
            width={15}
            height={20}
            alt="뒤로가기"
            onClick={() => {
              router.back();
            }}
          />
          <div>설정</div>
        </div>
        <div className={style.SettingMain}>
          {memberInfo.role === "SELLER" && (
            <>
              <div className={style.SideSimple}>
                <div>대표 이미지 변경</div>
                <Image src="/img/btn/right-btn.png" width={15} height={20} alt="이동" />
              </div>
              <div className={style.SideSimple}>
                <div>배달 가능 지역 변경</div>
                <Image src="/img/btn/right-btn.png" width={15} height={20} alt="이동" />
              </div>
            </>
          )}
          <div className={style.SideDetail}>
            <div>
              <div>알림 설정</div>
              {alarmState ? (
                //true일때 토클 이미지
                <Image src="/img/btn/right-btn.png" width={30} height={15} alt="토글On" />
              ) : (
                //false일때 토글 이미지
                <Image src="/img/btn/right-btn.png" width={30} height={15} alt="토글Off" />
              )}
            </div>
            <div>알림 설정을 ON 하실경우 카카오톡 을 통한 플리에 대한 알림을 받을 수 있습니다</div>
          </div>
          {memberInfo.role === "SELLER" && (
            <div className={style.SideDetail} onClick={userChangModalHandler}>
              <div>
                <div>사용자로 전환하기</div>
                <div>
                  <Image src="/img/btn/right-btn.png" width={15} height={20} alt="이동" />
                </div>
              </div>
              <div>사용자로 전환시 등록하신 판매자 관련 데이터는 사라집니다</div>
            </div>
          )}
          {memberInfo.role === "USER" && (
            <div className={style.SideDetail} onClick={userChangModalHandler}>
              <div>
                <div>판매자로 전환하기</div>
                <div>
                  <Image src="/img/btn/right-btn.png" width={15} height={20} alt="이동" />
                </div>
              </div>
              <div>판매자로 전환시 사업자 등록 및 추가적인 정보를 입력하셔야 합니다</div>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default MypageSetting;