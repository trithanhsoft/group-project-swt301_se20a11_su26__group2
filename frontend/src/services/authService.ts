const BASE_URL = 'http://localhost:8080/nonstopcoding';

export interface LoginResponse {
  status: number;
  code: number;
  message: string;
  result: {
    id: number;
    username: string;
    displayName: string;
    avatarUrl?: string;
    email: string;
    balance?: number;
    roles?: string[];
    status?: string;
    lockReason?: string;
    lockAppeal?: string;
  };
}

export const authService = {
  async login(username: string, password: string): Promise<LoginResponse['result']> {
    const response = await fetch(`${BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // CRITICAL: Required to receive and send HttpOnly cookies
      body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Tên đăng nhập hoặc mật khẩu không chính xác');
    }

    const data: LoginResponse = await response.json();
    return data.result;
  },

  async googleLogin(idToken: string): Promise<LoginResponse['result']> {
    const response = await fetch(`${BASE_URL}/auth/google`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // CRITICAL: Required to receive and send HttpOnly cookies
      body: JSON.stringify({ idToken }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Đăng nhập bằng Google thất bại');
    }

    const data: LoginResponse = await response.json();
    return data.result;
  },

  async register(registerData: any): Promise<LoginResponse['result']> {
    const response = await fetch(`${BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // CRITICAL: Required to receive and send HttpOnly cookies
      body: JSON.stringify(registerData),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Đăng ký không thành công');
    }

    const data: LoginResponse = await response.json();
    return data.result;
  },

  async logout(): Promise<void> {
    const response = await fetch(`${BASE_URL}/auth/logout`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // CRITICAL: Required to send and clear HttpOnly cookies
    });

    if (!response.ok) {
      throw new Error('Đăng xuất không thành công');
    }
  },

  async changePassword(changePasswordData: any): Promise<void> {
    const response = await fetch(`${BASE_URL}/me/change-password`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // CRITICAL: Required to send HttpOnly cookies
      body: JSON.stringify(changePasswordData),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Thay đổi mật khẩu không thành công');
    }
  },

  async getMyInfo(): Promise<any> {
    const response = await fetch(`${BASE_URL}/me/my-info`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // CRITICAL: Required to send HttpOnly cookies
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Không thể lấy thông tin người dùng');
    }

    const data = await response.json();
    return data.result;
  },

  async submitAppeal(appealReason: string): Promise<void> {
    const response = await fetch(`${BASE_URL}/me/appeal`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify({ appealReason }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to submit appeal');
    }
  },

  async refresh(): Promise<LoginResponse['result']> {
    const response = await fetch(`${BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // CRITICAL: Required to send and receive HttpOnly cookies
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to refresh token');
    }

    const data: LoginResponse = await response.json();
    return data.result;
  }
};

