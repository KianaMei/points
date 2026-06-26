import request from '@/config/axios'
import type {
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult
} from '@/api/clubpoints/shared/types'

export interface AppLedgerSummaryRespVO {
  availablePoints: number
  frozenPoints: number
  totalPoints: number
  annualClearedPoints?: number
  [key: string]: any
}

export interface AppLedgerTransactionRespVO extends ClubPointBaseRespVO {
  transactionNo?: string
  direction: number
  points: number
  sourceType?: number
  sourceId?: number
  issuingClubId?: number
  issuingClubNameSnapshot?: string
  reason?: string
}

export interface AppLedgerTransactionPageReqVO extends ClubPointPageParam {
  direction?: number
  sourceType?: number
  clubId?: number
  createTime?: string[]
}

const PREFIX = '/clubpoints/app/ledger'

export const getAppLedgerSummary = async (): Promise<AppLedgerSummaryRespVO> => {
  return await request.get({ url: `${PREFIX}/summary` })
}

export const getAppLedgerPage = async (
  params: AppLedgerTransactionPageReqVO
): Promise<ClubPointPageResult<AppLedgerTransactionRespVO>> => {
  return await request.get({ url: `${PREFIX}/page`, params })
}
