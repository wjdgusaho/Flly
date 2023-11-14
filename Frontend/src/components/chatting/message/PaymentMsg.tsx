import { useState, useEffect } from "react";
import style from "./style/PaymentMsg.module.css";
import Image from "next/image";
import axios from "axios";
import { tokenHttp } from "@/api/tokenHttp";
import { useRouter } from "next/router";

import { ToastErrorMessage } from "@/model/toastMessageJHM";
import { useSetRecoilState } from "recoil";
import { paymentInfoRecoil } from "@/recoil/paymentRecoil";

type PaymentInfo = {
  requestId: number;
  sellerId: number;
  sellerName: string;
  price: number;
  isPaid: boolean;
};

type PaymentMsgProps = {
  chattingId: number;
};

const PaymentMsg: React.FC<PaymentMsgProps> = ({ chattingId }) => {
  const router = useRouter();
  const [paymentInfo, setPaymentInfo] = useState<PaymentInfo>();
  const setPaymentInfoRecoil = useSetRecoilState(paymentInfoRecoil);

  useEffect(() => {
    tokenHttp
      .get(`/chatting/price/${chattingId}`)
      .then((response) => {
        if (response.data.code === 200) {
          setPaymentInfo(response.data.data);
          //요거 필수!! (엑세스 토큰 만료로 재발급 받았다면 바꿔줘!! )
          if (response.headers.authorization) {
            localStorage.setItem("accessToken", response.headers.authorization);
          }
        }
      })
      .catch((err) => {
        if (err.response.status === 403) {
          router.push("/fllylogin");
        }
      });

    // axios.get(`https://flower-ly.co.kr/api/chatting/price/${chattingId}`).then((response) => {
    //   // console.log(response);
    //   setPaymentInfo(response.data.data);
    // });
  }, []);

  const payAxiosHandler = () => {
    // console.log("카카오페이 버튼 클릭");
    // console.log(window.location.origin);

    const body = {
      domain: window.location.origin,
      requestId: paymentInfo?.requestId,
      sellerId: paymentInfo?.sellerId,
      price: paymentInfo?.price,
    };
    tokenHttp
      .post(`/chatting/kakaopay`, body)
      .then((response) => {
        if (response.data.code === 200) {
          const isMobile = /Mobi/i.test(window.navigator.userAgent); // 모바일인지 pc인지
          const resData = response.data.data;

          setPaymentInfoRecoil({
            chattingId: chattingId,
            paymentId: resData.paymentId,
          });
          window.location.href = isMobile
            ? resData.nextRedirectMobileUrl
            : resData.nextRedirectPcUrl;

          //요거 필수!! (엑세스 토큰 만료로 재발급 받았다면 바꿔줘!! )
          if (response.headers.authorization) {
            localStorage.setItem("accessToken", response.headers.authorization);
          }
        } else if (response.data.code == "-604") {
          ToastErrorMessage("이미 진행중인 주문이 있습니다.");
          //요거 필수!! (엑세스 토큰 만료로 재발급 받았다면 바꿔줘!! )
          if (response.headers.authorization) {
            localStorage.setItem("accessToken", response.headers.authorization);
          }
        }
      })
      .catch((err) => {
        if (err.response.status === 403) {
          router.push("/fllylogin");
        }
      });
  };

  return (
    <>
      <div className={style.mainBox}>
        <div className={style.title}>결제를 요청하였습니다.</div>
        <div className={style.infoDiv}>
          <div className={style.sellerDiv}>{paymentInfo?.sellerName}</div>
          <div className={style.priceDiv}>
            <div>금액</div>
            <div>{paymentInfo?.price.toLocaleString()} 원</div>
          </div>
          {paymentInfo?.isPaid ? (
            <div className={style.disabledBtnDiv}>결제 완료</div>
          ) : (
            <div
              className={style.btnDiv}
              onClick={() => {
                payAxiosHandler();
              }}
            >
              <Image src="/img/btn/kakao-pay-btn.png" width={80} height={30} alt="카카오페이" />
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default PaymentMsg;
