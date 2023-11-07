import React, { useEffect, useState } from "react";
import style from "./BuyerCardOne.module.css";
import ProgressBar from "./ProgressBar";
import Image from "next/image";
import { useRouter } from "next/router";
import { useSetRecoilState } from "recoil";
import { FllylistDiscRecoil } from "@/recoil/kdmRecoil";
import Cancel from "./Cancel";
import { ToastErrorMessage } from "@/model/toastMessageJHM";

type BuyerCardOneProps = {
  card: BuyerCard;
};

const stateProps = ["입찰", "조율", "주문완료", "제작완료", "픽업/배달완료"];

const BuyerCardOne = ({ card }: BuyerCardOneProps) => {
  const [cancelModal, setCancelModal] = useState(false); // 취소 모달
  const route = useRouter();
  const isClient = typeof window !== "undefined";
  const [windowWidth, setWindowWidth] = useState<number>(0);
  const stepNumber = stateProps.indexOf(card.state);
  const setCardProps = useSetRecoilState(FllylistDiscRecoil);

  const buttomBtnCmd = (stepNumber: number) => {
    switch (stepNumber) {
      case 0:
      case 1:
        return (
          <div className={style.cardBtn}>
            <button className={style.fllistBtn} onClick={() => fllistBtn(card.fllyId)}>
              플리스트
            </button>
            <button className={style.cancelBtn} onClick={() => handleCancel()}>
              취소하기
            </button>
          </div>
        );
      default:
        return (
          <div className={style.cardBtn}>
            <button
              className={style.fllistBtnClose}
              onClick={() => ToastErrorMessage("주문 이후에는 플리스트가 비활성화됩니다.")}
            >
              플리스트
            </button>
            <button className={style.cancelBtn} onClick={() => pageMoveHandelr()}>
              주문서보기
            </button>
          </div>
        );
    }
  };

  const posFlowerLeft = (step: number) => {
    if (step === 0) {
      return "1%";
    }
    if (step === 1) {
      return "16%";
    }
    if (step === 2) {
      return "34%";
    }
    if (step === 3) {
      return "54%";
    }
    if (step === 4) {
      return "87%";
    }
  };

  const mapColorNameToRGB = (colorName: string) => {
    switch (colorName) {
      case "빨간색":
        return "#DB4455";
      case "주황색":
        return "#F67828";
      case "분홍색":
        return "#FFC5BF";
      case "노랑색":
        return "#FBE870";
      case "파랑색":
        return "#0489DD";
      case "보라색":
        return "#CE92D8";
      case "흰색":
        return "#ffffff";
      case "선택안함":
        return "rgba(255, 255, 255, 0)";
      default:
        return "rgba(255, 255, 255, 0)"; // 만약 매핑되지 않는 경우 원래 문자열 반환
    }
  };

  const mapFlowerText = (colorName: string) => {
    switch (colorName) {
      case "빨간색":
        return "빨간";
      case "주황색":
        return "주황";
      case "분홍색":
        return "분홍";
      case "노랑색":
        return "노랑";
      case "파랑색":
        return "파랑";
      case "보라색":
        return "보라";
      case "흰색":
        return "흰색";
      default:
        return "없음"; // 만약 매핑되지 않는 경우 원래 문자열 반환
    }
  };

  const pageMoveHandelr = () => {
    if (card.fllyId) {
      route.push(
        {
          pathname: "/flly/order/sheet/[fllyId]",
          query: { fllyId: card.fllyId },
        },
        "/flly/detail", // 이것은 브라우저 주소창에 표시될 URL입니다.
        { shallow: true },
      );
    } else {
      ToastErrorMessage("잠시후 다시 눌러주세요!");
    }
  };

  const fllistBtn = (fllyId: number) => {
    setCardProps(card);
    route.push({
      pathname: `/list/buyer/[fllyId]/`,
      query: {
        fllyId: fllyId,
      },
    });
  };

  const handleCancel = () => {
    setCancelModal((pre) => !pre);
  };

  useEffect(() => {
    if (isClient) {
      // 현재의 화면 너비 설정
      setWindowWidth(window.innerWidth);

      // 창 크기가 변경될 때마다 화면 너비 업데이트
      const handleResize = () => {
        setWindowWidth(window.innerWidth);
      };
      window.addEventListener("resize", handleResize);

      return () => {
        window.removeEventListener("resize", handleResize);
      };
    }
  }, [isClient]);

  return (
    <div className={style.cardBox}>
      <div style={{ height: "40px" }}>
        <Image
          src="/img/icon/currentFlower.png"
          alt="현재상태"
          width={45}
          height={45}
          className={style.currentFlower}
          style={{ marginLeft: posFlowerLeft(stepNumber) }}
        />
      </div>
      <ProgressBar currentStep={stepNumber} />
      <div className={style.cardInfo}>
        <div className={style.cardImgBox}>
          <Image
            src="https://neighbrew.s3.ap-northeast-2.amazonaws.com/FlOWER/f82c544a-7f65-4879-9293-76ceaba5a6d2069_pink_peony.jpg.jpg"
            alt="꽃 이미지"
            fill
          />
        </div>
        <div className={style.infoText}>
          <div className={style.infoTitle}>상품정보</div>
          <div className={style.infoTable}>
            <div className={style.infoTitle}>상황</div>
            <div className={style.info}>{card.situation}</div>
          </div>
          <div className={style.infoTable}>
            <div className={style.infoTitle}>대상</div>
            <div className={style.info}>{card.target}</div>
          </div>
          <div className={style.infoColorTable}>
            <div>주요색상</div>
            {card.selectedColor.map((color, idx) => {
              const rgbColor = mapColorNameToRGB(color);
              return (
                <div
                  key={idx}
                  className={`${style.colorInfo} ${style.colorfirst}`}
                  style={{
                    color: isClient && windowWidth <= 426 ? rgbColor : "black",
                    backgroundColor: isClient && windowWidth <= 426 ? rgbColor : "var(--moregray)",
                    // backgroundColor: isClient && windowWidth <= 426 ? "" : "var(--moregray)",
                  }}
                >
                  {windowWidth <= 426 ? "" : mapFlowerText(color)}
                  {/* {windowWidth <= 426 ? "" : ""} */}
                </div>
                // <Image
                //   src={`/img/flowerColor/${mapFlowerColor(color)}.png`}
                //   alt="꽃 색상"
                //   width={40}
                //   height={40}
                //   key={idx}
                // />
              );
            })}
          </div>
          <div className={style.infoTable}>
            <div className={style.infoTitle}>꽃집</div>
            <div className={`${style.flowerShop}`}>{card.shopName}</div>
          </div>
        </div>
      </div>
      {buttomBtnCmd(stepNumber)}
      {cancelModal && <Cancel onCancel={handleCancel} />}
    </div>
  );
};

export default BuyerCardOne;
