import request from '@/config/axios'

export interface AppDashboardSummaryRespVO {
  availablePoints: number
  frozenPoints: number
  totalPoints: number
  clubCount: number
  todoCount: number
  unreadNotifyCount: number
  [key: string]: any
}

export const getAppDashboardSummary = async (): Promise<AppDashboardSummaryRespVO> => {
  return await request.get({ url: '/clubpoints/app/dashboard/summary' })
}
