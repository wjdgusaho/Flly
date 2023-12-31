import React, { useEffect, useState } from "react";
import style from "./BuyerFllyListProgressCard.module.css";
import Image from "next/image";
import BuyerFllyListProgressBar from "./BuyerFllyListProgressBar";
import Router from "next/router";

interface BuyerFillListType {
  fllyId: number;
  buyerNickName: string;
  deliveryPickupTime: string;
  progress: string;
  storeName: string;
  fllyOrderType: string;
  requestOrderType: string;
  isReviewed: boolean;
  imageUrls: string;
}

interface Props {
  $fllyInfo: BuyerFillListType;
}

const BuyerFllyListProgressCard = ({ $fllyInfo }: Props) => {
  const [progressStep, setProgressStep] = useState<number>(0);

  useEffect(() => {
    //나중에 백에서 들어올값
    const progress: string = $fllyInfo.progress;

    if (progress === "입찰") setProgressStep(0);
    else if (progress === "조율") setProgressStep(1);
    else if (progress === "주문완료") setProgressStep(2);
    else if (progress === "제작완료") setProgressStep(3);
    else if (progress === "픽업/배달완료") setProgressStep(4);
    /* eslint-disable-next-line */
  }, []);

  const handleFllyList = () => {
    Router.push("/list");
  };

  const handleFllyDetail = () => {
    Router.push(`/flly/detail/${$fllyInfo.fllyId}`);
  };

  const handleOrderDetail = () => {
    Router.push(`/flly/order/sheet/${$fllyInfo.fllyId}`);
  };

  return (
    <>
      <div className={style.cardBack}>
        <div className={style.cardHeader}>
          <div
            className={style.ImgBox}
            style={{ backgroundImage: `url('${$fllyInfo.imageUrls}')` }}
          />
          <div className={style.InfoBox}>
            <div className={style.OrderAddBox}>
              {progressStep >= 2 ? (
                <div onClick={() => handleOrderDetail()}>
                  주문서 보기 <span> &gt;</span>
                </div>
              ) : (
                <div onClick={() => handleFllyDetail()}>
                  의뢰서 보기 <span> &gt;</span>
                </div>
              )}
            </div>
            <div className={style.OrderInfoBox}>
              <div className={style.OrderInfoBoxHarf}>
                <div>주문자</div>
                <div>{$fllyInfo.buyerNickName}</div>
              </div>
              <div className={style.OrderInfoBoxHarf}>
                <div>구매처</div>
                <div>{progressStep >= 2 ? $fllyInfo.storeName : "구매처 선택중"}</div>
              </div>
              <div className={style.OrderInfoBoxHarf}>
                <div>주문유형</div>
                <div>{$fllyInfo.fllyOrderType}</div>
              </div>
            </div>
            <div className={style.OrderFooter} onClick={() => handleFllyList()}>
              <div>진행중 플리 이동</div>
            </div>
          </div>
        </div>
        <div>
          <BuyerFllyListProgressBar currentStep={progressStep} />
        </div>
      </div>
    </>
  );
};

export default BuyerFllyListProgressCard;
