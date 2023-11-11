import React, { useState } from "react";
import style from "./MypageReviewCard.module.css";
import Image from "next/image";
import { useRecoilValue } from "recoil";
import { MemberInfo, memberInfoState } from "@/recoil/memberInfoRecoil";

interface ReviewType {
  reviewId: number;
  requestId: number;
  storeName: string;
  content: string;
  createdAt: string;
}

interface Props {
  ModalChangeHandler: () => void;
  SelectIdChangeHandler: (reviewId: number, index: number) => void;
  $requestIndex: number;
  $reviewInfo: ReviewType;
}

const MypageReviewCard = ({
  ModalChangeHandler,
  SelectIdChangeHandler,
  $requestIndex,
  $reviewInfo,
}: Props) => {
  const memberInfo = useRecoilValue<MemberInfo>(memberInfoState);

  const DeleteBtnHandler = () => {
    ModalChangeHandler();
    SelectIdChangeHandler($reviewInfo.reviewId, $requestIndex);
  };

  return (
    <>
      <div className={style.ReviewCardBack}>
        {memberInfo.role === "USER" ? (
          <div className={style.BuyerReviewCardHeader}>
            <div>
              행복한 꽃집
              <span>
                <Image src="/img/btn/right-btn.png" width={10} height={15} alt="이동"></Image>
              </span>
            </div>
            <div className={style.BuyerReviewDelete} onClick={DeleteBtnHandler}>
              삭제
            </div>
          </div>
        ) : (
          <div className={style.SellerReviewCardHeader}>
            <div>김동민</div>
          </div>
        )}
        <div className={style.ReviewCardMain}>
          진짜 너무 잘해주셔서 너무 좋았어요 11111111111111111 앞으
        </div>
        <div className={style.ReviewCardFooter}>
          <div>2023.11.15</div>
        </div>
      </div>
    </>
  );
};

export default MypageReviewCard;