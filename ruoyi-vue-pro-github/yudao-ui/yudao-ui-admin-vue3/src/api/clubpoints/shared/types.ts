export const CLUB_POINT_REVIEW_APPROVED = 1
export const CLUB_POINT_REVIEW_REJECTED = 2

export type ClubPointReviewResult =
  | typeof CLUB_POINT_REVIEW_APPROVED
  | typeof CLUB_POINT_REVIEW_REJECTED

export enum ClubPointAttachmentType {
  FILE = 1,
  LINK = 2
}

export interface AttachmentInputVO {
  type: ClubPointAttachmentType
  name?: string
  fileId?: number | string
  url?: string
  remark?: string
}

export interface StrongConfirmPayload {
  confirmText: string
  confirmedAt: string
  reason?: string
}

export interface ReviewReqVO {
  id?: number
  approved?: boolean
  result?: ClubPointReviewResult
  reason?: string
}

export interface ClubPointRuleVersionOption {
  id: number
  versionNo?: string
  name: string
  status?: number
}

export interface ClubPointRuleItemOption {
  id: number
  ruleVersionId: number
  itemCode: string
  itemName: string
  minPoints?: number
  maxPoints?: number
  defaultPoints?: number
  status?: number
}

export interface ClubPointClubOption {
  id: number
  name: string
  code?: string
  status?: number
}

export type ClubPointDateTime = string | Date

export interface ClubPointPageParam {
  pageNo: number
  pageSize: number
  [key: string]: any
}

export interface ClubPointPageResult<T> {
  list: T[]
  total: number
}

export interface ClubPointIdReqVO {
  id: number
}

export interface ClubPointReasonReqVO extends ClubPointIdReqVO {
  reason?: string
}

export interface ClubPointStrongConfirmReqVO extends ClubPointReasonReqVO {
  strongConfirm: StrongConfirmPayload
}

export interface ClubPointBaseRespVO {
  id: number
  createTime?: ClubPointDateTime
  updateTime?: ClubPointDateTime
  [key: string]: any
}
