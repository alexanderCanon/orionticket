export interface AuthResponse {
  accessToken: string;
  userId: string;
  roleId: string;
}

export interface UserTokenPayload {
  sub: string;
  email: string;
  fullName?: string;
  roleId: string;
  organizerId?: string;
  exp: number;
}
