import type { OrderLine } from './OrderLine';

export type OrderStatus = 'PENDING' | 'PREPARING' | 'READY' | 'COMPLETED';

export interface Order {
  orderId: string;
  status: OrderStatus;
  totalAmount: number;
  itemCount: number;
  orderLines: OrderLine[];
}