// src/app/model/user-info.ts
export interface UserInfo {
  id: number;       // ← nuevo
  username: string;
  email: string;
  roles: string[];
}
