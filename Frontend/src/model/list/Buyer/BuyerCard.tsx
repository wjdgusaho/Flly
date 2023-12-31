// type BuyerCard = {
//   fllyId: number;
//   state: string;
//   img: string;
//   situation: string;
//   target: string;
//   selectedColor: string[];
//   shopName: string;
// };

type BuyerCardStong = {
  card: BuyerCard;
};

type BuyerCard = {
  budget: number;
  color1: string;
  color2: string;
  color3: string;
  consumer: string;
  deadline: string;
  fllyId: number;
  flower1: flower;
  flower2: flower;
  flower3: flower;
  imageUrl: string;
  orderType: string;
  progress: string;
  requestAddress: string | null;
  requestContent: string;
  situation: string;
  target: string;
  storeName: string;
};

type flower = {
  flowerName: string;
  meaning: string;
};
