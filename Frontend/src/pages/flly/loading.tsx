import React, { useEffect, useState } from "react";
import style from "@/components/flly/fllyUser/FllyLoading.module.css";
import Image from "next/image";
import { useRecoilValue, useRecoilState } from "recoil";
import { flowerState, randomFlowerState } from "@/recoil/fllyRecoil";
import OpenAI from "openai";
import { bouquetsState, bouquetType } from "@/recoil/fllyRecoil";
import { useRouter } from "next/router";

const FllyLoading = () => {
  const router = useRouter();
  const [imgList, setImgList] = useState<bouquetType[]>([]);
  const [order, setOrder] = useState<string>("");

  const apikey = process.env.OPENAI_API_KEY;
  const openai = new OpenAI({
    apiKey: apikey,
    dangerouslyAllowBrowser: true,
  });

  const flowers = useRecoilValue(flowerState);
  const randoms = useRecoilValue(randomFlowerState);
  const [bouquets, setBouquets] = useRecoilState(bouquetsState);

  const generateOrder = async () => {
    if (flowers.length > 0) {
      const flowerStringArray = flowers.map((flower) => {
        return `${flower.color} ${flower.engName}`;
      });
      const flowerString = flowerStringArray.join(", ");
      setOrder(
        `a pretty and beautiful bouquet wrapped in paper of ${flowerString}, on Light Bluish Gray background, Place the bouquet in the middle, more distant, overall,`,
      );
    } else {
      const flowerStringArray = randoms.map((flower) => {
        return `${flower.color} ${flower.engName}`;
      });
      const flowerString = flowerStringArray.join(", ");
      setOrder(
        `“a pretty and beautiful bouquet wrapped in paper of ${flowerString}, on Light Bluish Gray background, Place the bouquet in the middle, more distant, overall,`,
      );
    }
  };

  const generateImage = async () => {
    try {
      const response = await openai.images.generate({
        model: "dall-e-2",
        prompt: order,
        n: 2,
        size: "1024x1024",
      });
      const NewImage: bouquetType[] = [];
      if (response) {
        response.data.forEach((image) => {
          if (image.url) NewImage.push({ url: image.url });
        });
        setImgList(NewImage);
      }
    } catch (error: any) {
      if (error.response) {
      } else {
      }
    }
  };

  useEffect(() => {
    if (imgList.length <= 0) {
      generateOrder();
    } else {
      setBouquets([...imgList, ...bouquets]);
    }
    /* eslint-disable-next-line */
  }, [imgList]);

  useEffect(() => {
    // router.push("bouquet"); // 지우기
    if (order != "") generateImage();
    /* eslint-disable-next-line */
  }, [order]);

  useEffect(() => {
    if (imgList.length !== 0) router.push("bouquet");
    /* eslint-disable-next-line */
  }, [bouquets]);

  return (
    <>
      <div className={style.fllyBox}>
        <div className={style.contentBox}>
          <div className={style.guide}>하나뿐인 꽃다발을 생성중입니다.</div>
          <Image src="/img/etc/loading.gif" width={200} height={200} alt="로딩"></Image>
        </div>
      </div>
    </>
  );
};

export default FllyLoading;
