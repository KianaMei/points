import request from '@/config/axios'
import type { ClubPointDashboardTodoItemVO } from '@/api/clubpoints/shared/types'

export interface LeaderDashboardSummaryRespVO {
  managedClubCount: number
  draftActivityCount: number
  rejectedActivityCount: number
  attendanceExceptionCount: number
  pendingContributionSubmitCount: number
  todoCount: number
  todoItems: ClubPointDashboardTodoItemVO[]
}

export const getLeaderDashboardSummary = async (): Promise<LeaderDashboardSummaryRespVO> => {
  return await request.get({ url: '/clubpoints/leader/dashboard/summary' })
}
