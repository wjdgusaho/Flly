import { atom } from "recoil";

export interface sidoDataType {
  sidoCode: number;
  sidoName: string;
}

export interface sigunguDataType {
  sigunguCode: number;
  sigunguName: string;
}

export interface dongDataType {
  dongCode: number;
  dongName: string;
}

export interface regionType {
  sidoCode: number;
  sigunguCode: number;
  dongCode: number;
}

export const regionState = atom<regionType[]>({
  key: "regionState",
  default: [],
});

export interface bouquetType {
  url: string;
}

export interface flowerCardType {
  flowerCode : number,
  imageUrl:  string,
  flowerName : string,
  color : string,
  engName : string
  meaning : string,
  colorName : string,
}

export const situationState = atom({
  key: "situationState",
  default: "",
});

export const targetState = atom({
  key: "targetState",
  default: "",
});

export const colorState = atom({
  key: "colorState",
  default: [] as string[],
});

export const flowerState = atom({
  key: "flowerState",
  default: [] as flowerCardType[],
});

export const bouquetsState = atom({
  key: 'bouquetsState',
  // default: [{url:""}, {url:""}] as bouquetType[],
  default: [] as bouquetType[],
});

export const bouquetState = atom({
  key: 'bouquetState',
  default: null as bouquetType | null,
});
