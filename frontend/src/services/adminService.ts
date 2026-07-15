const BASE_URL = 'http://localhost:8080/nonstopcoding';

// Helper: tự động refresh token khi gặp 401, rồi retry lại request (có queue để tránh race condition khi gọi nhiều API song song)
let isRefreshing = false;
let refreshSubscribers: (() => void)[] = [];

function subscribeTokenRefresh(cb: () => void) {
  refreshSubscribers.push(cb);
}

function onRefreshed() {
  refreshSubscribers.forEach(cb => cb());
  refreshSubscribers = [];
}

function onRefreshFailed() {
  refreshSubscribers.forEach(cb => cb());
  refreshSubscribers = [];
}

async function fetchWithAutoRefresh(input: RequestInfo, init?: RequestInit): Promise<Response> {
  let response = await fetch(input, init);

  if (response.status === 401) {
    // Không refresh nếu đây chính là request refresh token
    const urlString = typeof input === 'string' ? input : (input as Request).url;
    if (urlString.includes('/auth/refresh')) {
      return response;
    }

    if (!isRefreshing) {
      isRefreshing = true;
      try {
        const refreshRes = await fetch(`${BASE_URL}/auth/refresh`, {
          method: 'POST',
          credentials: 'include',
        });
        if (refreshRes.ok) {
          isRefreshing = false;
          onRefreshed();
          return fetch(input, init);
        } else {
          isRefreshing = false;
          onRefreshFailed();
          console.warn('[Auth] Refresh token hết hạn, cần đăng nhập lại.');
          localStorage.removeItem('user_info');
          window.location.href = '/login';
        }
      } catch (err) {
        isRefreshing = false;
        onRefreshFailed();
        console.warn('[Auth] Không thể refresh token:', err);
      }
    } else {
      return new Promise<Response>((resolve) => {
        subscribeTokenRefresh(() => {
          resolve(fetch(input, init));
        });
      });
    }
  }

  return response;
}


export interface AdminDashboardStats {
  totalRevenue: number;
  activeUsers: number;
  activeContests: number;
  totalCourses: number;
  totalInstructors: number;
  totalProblems: number;
  financialChartData?: { label: string; amount: number; count: number; usersCount: number }[];
  topCategories?: { name: string; count: number; color: string }[];
  topCourses?: { name: string; instructor: string; count: number; color: string }[];
  topInstructors?: { name: string; count: number; color: string }[];
  topProblems?: { name: string; difficulty: string; count: number; color: string }[];
}

export interface MonthlyFinancialRecord {
  label: string;
  datePrefix: string;
  gross: number;
  count: number;
  rewards: number;
  server: number;
  marketing: number;
}

export interface TopRevenueCourse {
  name: string;
  tutor: string;
  sold: number;
  gross: number;
  payout: number;
  plat: number;
}


export interface OrderDetails {
  id: string;
  customerName: string;
  customerEmail: string;
  courses: string;
  grossAmount: number;
  instructorShare: number;
  platformCut: number;
  date: string;
}

export interface PayoutDetails {
  id: string;
  instructorName: string;
  instructorEmail: string;
  amount: number;
  bankAccount: string;
  status: 'COMPLETED' | 'PENDING' | 'FAILED';
  date: string;
}

export interface AwardDetails {
  id: string;
  userName: string;
  userEmail: string;
  amount: number;
  date: string;
  referenceId: string;
}

export interface SaleDetails {
  orderId: string;
  courseTitle: string;
  instructorName: string;
  customerName: string;
  price: number;
  date: string;
}

export interface MonthlyFinancialBreakdown {
  label: string;
  datePrefix: string;
  gross: number;
  count: number;
  rewards: number;
  server: number;
  marketing: number;
  netProfit: number;
}

export interface AdminFinancialDetails {
  orders: OrderDetails[];
  awards: AwardDetails[];
  sales: SaleDetails[];
  monthlyBreakdowns: MonthlyFinancialBreakdown[];
}

export interface AdminCourse {
  id: string;
  instructorId: number;
  instructorName: string;
  title: string;
  thumbnailUrl: string;
  shortDescription: string;
  longDescription: string;
  status: 'PENDING_AI' | 'PENDING_ADMIN' | 'APPROVED' | 'REJECTED';
  price: number;
  averageRating: number;
  totalReviews: number;
  totalEnrolled: number;
  totalLessons: number;
  totalQuizzes: number;
  totalVideos: number;
  totalChapters: number;
  instructorAvatarUrl?: string;
}

export interface AdminInstructorApplication {
  id: number;
  userId: number;
  fullName: string;
  email: string;
  cvUrl: string;
  introduction: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'AI_REJECTED';
  adminNote?: string;
  aiScore?: number;
  aiSummary?: string;
  aiSpecialization?: string;
  aiTechnologies?: string;
  aiExperienceYears?: number;
  aiStrengths?: string;
  aiWeaknesses?: string;
  aiRecommendation?: string;
  createdAt: string;
}

export interface AdminInstructor {
  id: number;
  userId: number;
  fullName: string;
  major: string;
  bio: string;
  status: 'ACTIVE' | 'SUSPENDED';
  coursesCount: number;
  rating: number;
  studentsCount: number;
}

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  registerDate: string;
  status: 'ACTIVE' | 'LOCKED';
  balance: number;
  totalDeposited: number;
  totalPurchased: number;
  purchasedCourses: { id: string; title: string; price: number; date: string }[];
  isOnline?: boolean;
  lockReason?: string;
  lockAppeal?: string;
}

export interface PageResponse<T> {
  page: number;
  size: number;
  numberOfElements: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  content: T[];
}

export interface AdminProblem {
  id: number;
  title: string;
  description: string;
  inputDescription: string;
  outputDescription: string;
  constraints: string;
  exampleInput: string;
  exampleOutput: string;
  hint: string;
  problemScope: 'LESSON' | 'CONTEST' | 'SHARED' | 'PRACTICE';
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  isActive: boolean;
  createdBy: number;
  createdAt: string;
  totalTestcases: number;
  timeLimitMs: number;
  memoryLimitKb: number;
  isPublic: boolean;
  score: number;
  solutions?: string;
  totalSubmissions: number;
  acceptedSubmissions: number;
  isDeleted: boolean;
  tags?: string[];
  starterTemplates?: Record<string, string>;
}

export interface AdminProblemTestcase {
  id?: number;
  problemId: number;
  inputData: string;
  expectedOutput: string;
  orderIndex: number;
  token?: string;
  scoreWeight?: number;
  isHidden?: boolean;
}

/*
let mockProblemTestcases: Record<number, AdminProblemTestcase[]> = {
  1: [
    { id: 101, problemId: 1, inputData: "nums = [2,7,11,15]\ntarget = 9", expectedOutput: "[0,1]", orderIndex: 0 },
    { id: 102, problemId: 1, inputData: "nums = [3,2,4]\ntarget = 6", expectedOutput: "[1,2]", orderIndex: 1 },
    { id: 103, problemId: 1, inputData: "nums = [3,3]\ntarget = 6", expectedOutput: "[0,1]", orderIndex: 2 },
    { id: 104, problemId: 1, inputData: "nums = [1,5,9,12]\ntarget = 14", expectedOutput: "[-1,-1]", orderIndex: 3 }
  ],
  2: [
    { id: 201, problemId: 2, inputData: "s = \"abcabcbb\"", expectedOutput: "3", orderIndex: 0 },
    { id: 202, problemId: 2, inputData: "s = \"bbbbb\"", expectedOutput: "1", orderIndex: 1 },
    { id: 203, problemId: 2, inputData: "s = \"pwwkew\"", expectedOutput: "3", orderIndex: 2 },
    { id: 204, problemId: 2, inputData: "s = \"\"", expectedOutput: "0", orderIndex: 3 },
    { id: 205, problemId: 2, inputData: "s = \"au\"", expectedOutput: "2", orderIndex: 4 }
  ],
  3: [
    { id: 301, problemId: 3, inputData: "nums1 = [1,3], nums2 = [2]", expectedOutput: "2.00000", orderIndex: 0 },
    { id: 302, problemId: 3, inputData: "nums1 = [1,2], nums2 = [3,4]", expectedOutput: "2.50000", orderIndex: 1 }
  ]
};
*/

export interface AdminContest {
  id: number;
  title: string;
  description: string;
  scoringRule: 'ICPC' | 'IOI' | 'CUSTOM';
  startTime: string;
  endTime: string;
  durations: number; // in minutes
  status: 'DRAFT' | 'UPCOMING' | 'ONGOING' | 'ENDED' | 'CANCELLED' | 'DELETED';
  participantCount: number;
  submissionCount: number;
  averageScore: number;
  password?: string;
  isDeleted?: boolean;
  databaseStatus?: string;
}

export interface ActivityLog {
  id: string;
  type: 'REGISTER' | 'DEPOSIT' | 'BUY_COURSE' | 'APPROVAL';
  user: string;
  detail: string;
  timestamp: string;
}

export interface AdminDepositHistory {
  id: string;
  userName: string;
  amount: number;
  date: string;
}

// Mock database to simulate stateful actions locally when backend is unavailable

let mockCourses: AdminCourse[] = [
  {
    id: "101",
    instructorId: 10,
    instructorName: "Dr. Jenkins",
    instructorAvatarUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80",
    title: "Mastering Full-Stack React & Node.js",
    thumbnailUrl: "https://images.unsplash.com/photo-1633356122544-f134324a6cee?auto=format&fit=crop&w=400&q=80",
    shortDescription: "Build scalable web applications from scratch using MERN stack.",
    longDescription: "This course is a comprehensive, deep-dive into standard React and Node.js. It covers everything from project initialization, routing, styling, state management, testing, and production deployment.",
    status: 'APPROVED',
    price: 499000,
    averageRating: 4.8,
    totalReviews: 120,
    totalEnrolled: 340,
    totalLessons: 42,
    totalQuizzes: 8,
    totalVideos: 24,
    totalChapters: 6,
  },
  {
    id: "102",
    instructorId: 11,
    instructorName: "Alice Miller",
    instructorAvatarUrl: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
    title: "Java Algorithms & Coding Arena",
    thumbnailUrl: "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=400&q=80",
    shortDescription: "Solve complex programmatic challenges using Java standard library.",
    longDescription: "Ideal for student developers preparing for technical coding interviews. Learn Big-O analysis, sorting algorithms, trees, graph theory, and dynamic programming.",
    status: 'APPROVED',
    price: 389000,
    averageRating: 4.6,
    totalReviews: 85,
    totalEnrolled: 210,
    totalLessons: 30,
    totalQuizzes: 5,
    totalVideos: 18,
    totalChapters: 4,
  },
  {
    id: "103",
    instructorId: 10,
    instructorName: "Dr. Jenkins",
    instructorAvatarUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80",
    title: "Python Data Science and Machine Learning",
    thumbnailUrl: "https://images.unsplash.com/photo-1527474305487-b87b222841cc?auto=format&fit=crop&w=400&q=80",
    shortDescription: "Analyze datasets, build neural networks, and visualize data trends.",
    longDescription: "Learn Python libraries including NumPy, Pandas, Scikit-Learn, and TensorFlow. Perfect for beginners entering the AI audit and science sectors.",
    status: 'PENDING_ADMIN',
    price: 599000,
    averageRating: 0.0,
    totalReviews: 0,
    totalEnrolled: 0,
    totalLessons: 35,
    totalQuizzes: 6,
    totalVideos: 20,
    totalChapters: 5,
  },
  {
    id: "104",
    instructorId: 12,
    instructorName: "John Doe",
    instructorAvatarUrl: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
    title: "Go Microservices & Dockerized Deployments",
    thumbnailUrl: "https://images.unsplash.com/photo-1607799279861-4dd421887fb3?auto=format&fit=crop&w=400&q=80",
    shortDescription: "Build blazing fast microservices with Golang, gRPC and RabbitMQ.",
    longDescription: "Learn to design production systems with distributed messaging, microservice gateways, and orchestration using Docker-compose.",
    status: 'PENDING_ADMIN',
    price: 650000,
    averageRating: 0.0,
    totalReviews: 0,
    totalEnrolled: 0,
    totalLessons: 28,
    totalQuizzes: 4,
    totalVideos: 15,
    totalChapters: 4,
  }
];

let mockInstructorApplications: AdminInstructorApplication[] = [
  {
    id: 1,
    userId: 201,
    fullName: "Elena Rostova",
    email: "elena@nonstopcoding.edu",
    cvUrl: "https://example.com/cv-elena.pdf",
    introduction: "Senior React Engineer with 8 years of experience. Former Tech Lead at Yandex. Passionate about mentoring.",
    status: 'PENDING',
    createdAt: "2026-06-01T10:30:00Z"
  },
  {
    id: 2,
    userId: 202,
    fullName: "Marcus Aurelius",
    email: "marcus.coder@gmail.com",
    cvUrl: "https://example.com/cv-marcus.pdf",
    introduction: "Core C++ compiler developer. I want to build a deep, high-level course on CPU architectures and assembly.",
    status: 'PENDING',
    createdAt: "2026-06-05T14:45:00Z"
  }
];

let mockInstructors: AdminInstructor[] = [];


let mockUsers: AdminUser[] = [
  {
    id: 101,
    name: "Nguyen Van A",
    email: "vana@gmail.com",
    registerDate: "2026-01-15T08:00:00Z",
    status: 'ACTIVE',
    balance: 1500000,
    totalDeposited: 5000000,
    totalPurchased: 3500000,
    purchasedCourses: [
      { id: "101", title: "Mastering Full-Stack React & Node.js", price: 499000, date: "2026-02-01T12:00:00Z" },
      { id: "102", title: "Java Algorithms & Coding Arena", price: 389000, date: "2026-03-10T14:20:00Z" }
    ],
    isOnline: true
  },
  {
    id: 102,
    name: "Tran Thi B",
    email: "thib@gmail.com",
    registerDate: "2026-02-20T10:15:00Z",
    status: 'ACTIVE',
    balance: 200000,
    totalDeposited: 1200000,
    totalPurchased: 1000000,
    purchasedCourses: [
      { id: "101", title: "Mastering Full-Stack React & Node.js", price: 499000, date: "2026-02-25T09:00:00Z" }
    ],
    isOnline: false
  },
  {
    id: 103,
    name: "Le Van C",
    email: "vanc@gmail.com",
    registerDate: "2026-03-05T16:30:00Z",
    status: 'LOCKED',
    balance: 0,
    totalDeposited: 300000,
    totalPurchased: 300000,
    purchasedCourses: [],
    isOnline: false
  },
  {
    id: 104,
    name: "Pham Minh D",
    email: "minhd@hotmail.com",
    registerDate: "2026-05-12T11:40:00Z",
    status: 'ACTIVE',
    balance: 4500000,
    totalDeposited: 4500000,
    totalPurchased: 0,
    purchasedCourses: [],
    isOnline: true
  }
];

/*
let mockProblems: AdminProblem[] = [
  {
    id: 1,
    title: "Two Sum",
    description: "Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.",
    inputDescription: "An integer array `nums` and a single target integer `target`.",
    outputDescription: "Indices of the two numbers in any order.",
    constraints: "-10^9 <= nums[i] <= 10^9\n-10^9 <= target <= 10^9",
    exampleInput: "nums = [2,7,11,15], target = 9",
    exampleOutput: "[0,1]",
    hint: "Use a hashmap to check if the complement (target - nums[i]) already exists in the map.",
    problemScope: "PRACTICE",
    difficulty: "EASY",
    isActive: true,
    createdBy: 1001,
    createdAt: "2026-01-20T10:00:00Z",
    totalTestcases: 4,
    timeLimitMs: 1000,
    memoryLimitKb: 128000,
    isPublic: true,
    score: 100.0,
    totalSubmissions: 1245,
    acceptedSubmissions: 850
  },
  {
    id: 2,
    title: "Longest Substring Without Repeating Characters",
    description: "Given a string `s`, find the length of the longest substring without repeating characters.",
    inputDescription: "A single string `s` containing english letters, digits, and symbols.",
    outputDescription: "An integer representing the length of the longest unique substring.",
    constraints: "0 <= s.length <= 5 * 10^4",
    exampleInput: "s = \"abcabcbb\"",
    exampleOutput: "3 (The substring is \"abc\")",
    hint: "Use the sliding window technique with two pointers.",
    problemScope: "PRACTICE",
    difficulty: "MEDIUM",
    isActive: true,
    createdBy: 1002,
    createdAt: "2026-02-15T15:30:00Z",
    totalTestcases: 5,
    timeLimitMs: 1500,
    memoryLimitKb: 256000,
    isPublic: true,
    score: 100.0,
    totalSubmissions: 540,
    acceptedSubmissions: 210
  },
  {
    id: 3,
    title: "Median of Two Sorted Arrays",
    description: "Given two sorted arrays nums1 and nums2 of size m and n respectively, return the median of the two sorted arrays.",
    inputDescription: "Two sorted integer arrays nums1 and nums2.",
    outputDescription: "A double representing the median.",
    constraints: "nums1.length == m, nums2.length == n",
    exampleInput: "nums1 = [1,3], nums2 = [2]",
    exampleOutput: "2.00000",
    hint: "Think about binary search partition.",
    problemScope: "CONTEST",
    difficulty: "HARD",
    isActive: true,
    createdBy: 1001,
    createdAt: "2026-03-01T12:00:00Z",
    totalTestcases: 12,
    timeLimitMs: 2000,
    memoryLimitKb: 128000,
    isPublic: false,
    score: 200.0,
    totalSubmissions: 310,
    acceptedSubmissions: 45
  },
  {
    id: 4,
    title: "Palindrome Number",
    description: "Given an integer x, return true if x is a palindrome, and false otherwise.",
    inputDescription: "An integer x.",
    outputDescription: "true or false.",
    constraints: "-2^31 <= x <= 2^31 - 1",
    exampleInput: "x = 121",
    exampleOutput: "true",
    hint: "Try reversing the second half of the number.",
    problemScope: "PRACTICE",
    difficulty: "EASY",
    isActive: false,
    createdBy: 1003,
    createdAt: "2026-03-10T14:00:00Z",
    totalTestcases: 0,
    timeLimitMs: 1000,
    memoryLimitKb: 64000,
    isPublic: false,
    score: 50.0,
    totalSubmissions: 0,
    acceptedSubmissions: 0
  },
  {
    id: 5,
    title: "Spring Security OAuth2 Validator",
    description: "Implement a parser/validator for Spring Security OAuth2 tokens.",
    inputDescription: "String representation of a token.",
    outputDescription: "Boolean validation state.",
    constraints: "Token length <= 2048",
    exampleInput: "token = \"bearer eyJ...\"",
    exampleOutput: "true",
    hint: "Check JWT header and signature structure.",
    problemScope: "CONTEST",
    difficulty: "HARD",
    isActive: false,
    createdBy: 1001,
    createdAt: "2026-03-12T08:30:00Z",
    totalTestcases: 0,
    timeLimitMs: 3000,
    memoryLimitKb: 256000,
    isPublic: false,
    score: 150.0,
    totalSubmissions: 0,
    acceptedSubmissions: 0
  }
];
*/

// let mockContests: AdminContest[] = [];

let mockActivityLogs: ActivityLog[] = [
  {
    id: "log-1",
    type: "REGISTER",
    user: "Nguyen Van A",
    detail: "Registered a new account successfully.",
    timestamp: "2026-06-07T07:10:00Z"
  },
  {
    id: "log-2",
    type: "DEPOSIT",
    user: "Tran Thi B",
    detail: "Deposited 500,000 ₫ via VNPay transaction code #VN9429.",
    timestamp: "2026-06-07T07:22:00Z"
  },
  {
    id: "log-3",
    type: "BUY_COURSE",
    user: "Nguyen Van A",
    detail: "Purchased course: 'Mastering Full-Stack React & Node.js'.",
    timestamp: "2026-06-07T07:45:00Z"
  },
  {
    id: "log-4",
    type: "APPROVAL",
    user: "Admin",
    detail: "Approved course application: 'Java Algorithms & Coding Arena'.",
    timestamp: "2026-06-07T07:50:00Z"
  }
];


// Helper to delay response for realism
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export const adminService = {
  cloneProblem: async (problemId: number) => {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/problems/${problemId}/clone`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include' // or 'include' based on authentication mechanism, let's use default if omitted
      });
      if (!response.ok) {
        throw new Error('Failed to clone problem');
      }
      return await response.json();
    } catch (error) {
      console.error('Error cloning problem:', error);
      throw error;
    }
  },

  // Statistics
  async getDashboardStats(): Promise<AdminDashboardStats> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/dashboard/stats`, { credentials: 'include' });
      if (response.ok) {
        const data = await response.json();
        return data.result;
      }
    } catch (err) {
      console.warn("API for Dashboard Stats failed:", err);
    }
    return null as any;
  },

  async getActivityLogs(): Promise<ActivityLog[]> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/dashboard/activity-logs`, { credentials: 'include' });
      if (response.ok) {
        const data = await response.json();
        return data.result;
      }
    } catch (err) {
      console.warn("API for Activity Logs failed:", err);
    }
    return [];
  },

  async getRecentDeposits(): Promise<AdminDepositHistory[]> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/dashboard/recent-deposits`, { credentials: 'include' });
      if (response.ok) {
        const data = await response.json();
        return data.result;
      }
    } catch (err) {
      console.warn("API for Recent Deposits failed:", err);
    }
    return [];
  },

  async getAllDeposits(): Promise<AdminDepositHistory[]> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/dashboard/all-deposits`, { credentials: 'include' });
      if (response.ok) {
        const data = await response.json();
        return data.result;
      }
    } catch (err) {
      console.warn("API for All Deposits failed:", err);
    }
    return [];
  },

  // Courses
  async getCourses(): Promise<AdminCourse[]> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/courses`, { credentials: 'include' });
      if (response.ok) {
        const data = await response.json();
        return data.result;
      }
    } catch (err) {
      console.warn("Using mock data for Admin Courses:", err);
    }
    await delay(300);
    return mockCourses;
  },

  async approveCourse(courseId: string, status: 'APPROVED' | 'REJECTED'): Promise<AdminCourse> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/courses/${courseId}/approve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status }),
        credentials: 'include'
      });
      if (response.ok) {
        const data = await response.json();
        return data.result;
      }
    } catch (err) {
      console.warn("Mocking approve course action:", err);
    }
    await delay(400);
    mockCourses = mockCourses.map(c => c.id === courseId ? { ...c, status } : c);
    const updated = mockCourses.find(c => c.id === courseId)!;
    // Add log
    mockActivityLogs.unshift({
      id: `log-${Date.now()}`,
      type: "APPROVAL",
      user: "Admin",
      detail: `${status === 'APPROVED' ? 'Approved' : 'Rejected'} course: '${updated.title}'`,
      timestamp: new Date().toISOString()
    });
    return updated;
  },

  // Instructor applications
  async getInstructorApplications(): Promise<AdminInstructorApplication[]> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/instructors/applications`, { credentials: 'include' });
      if (response.ok) {
        const data = await response.json();
        return data.result;
      }
    } catch (err) {
      console.warn("API for Instructor Applications failed:", err);
    }
    return [];
  },

  async approveInstructorApplication(appId: number, status: 'APPROVED' | 'REJECTED', adminNote?: string): Promise<AdminInstructorApplication> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/instructors/applications/${appId}/approve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status, adminNote }),
        credentials: 'include'
      });
      if (response.ok) {
        const data = await response.json();
        return data.result;
      }
    } catch (err) {
      console.warn("Mocking approve instructor application:", err);
    }
    await delay(400);
    mockInstructorApplications = mockInstructorApplications.map(app => 
      app.id === appId ? { ...app, status, adminNote } : app
    );
    const app = mockInstructorApplications.find(a => a.id === appId)!;
    if (status === 'APPROVED') {
      // Add instructor to active list
      mockInstructors.push({
        id: mockInstructors.length + 13,
        userId: app.userId,
        fullName: app.fullName,
        major: "Software Engineering Instructor",
        bio: app.introduction,
        status: 'ACTIVE',
        coursesCount: 0,
        rating: 5.0,
        studentsCount: 0
      });
    }
    // Add log
    mockActivityLogs.unshift({
      id: `log-${Date.now()}`,
      type: "APPROVAL",
      user: "Admin",
      detail: `${status === 'APPROVED' ? 'Approved' : 'Rejected'} instructor application from '${app.fullName}'`,
      timestamp: new Date().toISOString()
    });
    return app;
  },

  async getInstructors(): Promise<AdminInstructor[]> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/instructors`, { credentials: 'include' });
      console.log('[DEBUG] GET /admin/instructors - status:', response.status, response.statusText);
      if (response.ok) {
        const data = await response.json();
        console.log('[DEBUG] /admin/instructors raw response:', data);
        console.log('[DEBUG] /admin/instructors result:', data.result);
        return data.result || [];
      } else {
        const errText = await response.text();
        console.warn('[DEBUG] /admin/instructors failed:', response.status, errText);
      }
    } catch (err) {
      console.warn('[DEBUG] /admin/instructors network error:', err);
    }
    await delay(300);
    return mockInstructors;
  },

  async setInstructorStatus(instructorId: number, status: 'ACTIVE' | 'SUSPENDED'): Promise<AdminInstructor> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/instructors/${instructorId}/status`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status }),
        credentials: 'include'
      });
      if (response.ok) {
        const data = await response.json();
        // Map backend AdminInstructorResponse to AdminInstructor
        const r = data.result;
        return {
          id: r.id,
          userId: r.userId,
          fullName: r.fullName,
          major: r.major || '',
          bio: r.bio || '',
          status: r.status as 'ACTIVE' | 'SUSPENDED',
          coursesCount: r.coursesCount || 0,
          rating: r.rating || 0,
          studentsCount: r.studentsCount || 0
        };
      }
    } catch (err) {
      console.warn('Mocking update instructor status:', err);
    }
    await delay(300);
    mockInstructors = mockInstructors.map(ins => ins.id === instructorId ? { ...ins, status } : ins);
    return mockInstructors.find(ins => ins.id === instructorId)!;
  },

  // Users List
  async getUsers(): Promise<AdminUser[]> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/users`, { credentials: 'include' });
      if (response.ok) {
        const data = await response.json();
        return data.result;
      }
    } catch (err) {
      console.warn("API for Users list failed:", err);
    }
    return [];
  },

  async setUserLockStatus(userId: number, status: 'ACTIVE' | 'LOCKED', reason?: string): Promise<AdminUser> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/users/${userId}/lock`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status, reason }),
        credentials: 'include'
      });
      if (response.ok) {
        const data = await response.json();
        return data.result;
      }
    } catch (err) {
      console.warn("Mocking user lock/unlock status:", err);
    }
    await delay(300);
    mockUsers = mockUsers.map(u => u.id === userId ? { ...u, status, lockReason: reason, lockAppeal: undefined } : u);
    const updated = mockUsers.find(u => u.id === userId)!;
    // Add log
    mockActivityLogs.unshift({
      id: `log-${Date.now()}`,
      type: "APPROVAL",
      user: "Admin",
      detail: `${status === 'LOCKED' ? 'Locked' : 'Unlocked'} account of '${updated.name}'`,
      timestamp: new Date().toISOString()
    });
    return updated;
  },

  // Problems
  async getProblems(): Promise<AdminProblem[]> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/problems`, { credentials: 'include' });
    if (!response.ok) {
      throw new Error('Failed to fetch admin problems');
    }
    const data = await response.json();
    return data.result;
  },

  async createProblem(problem: Omit<AdminProblem, 'id' | 'createdAt' | 'createdBy' | 'isActive' | 'totalSubmissions' | 'acceptedSubmissions'>): Promise<AdminProblem> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/problems`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(problem),
      credentials: 'include'
    });
    if (!response.ok) {
      throw new Error('Failed to create problem');
    }
    const data = await response.json();
    return data.result;
  },

  async updateProblemScope(problemId: number, problemScope: 'LESSON' | 'CONTEST' | 'SHARED' | 'PRACTICE'): Promise<AdminProblem> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/problems/${problemId}/scope`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ problemScope }),
      credentials: 'include'
    });
    if (!response.ok) {
      throw new Error('Failed to update problem scope');
    }
    const data = await response.json();
    return data.result;
  },

  async updateProblemPublicStatus(problemId: number, isPublic: boolean): Promise<AdminProblem> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/problems/${problemId}/public`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ isPublic }),
      credentials: 'include'
    });
    if (!response.ok) {
      throw new Error('Failed to update problem public status');
    }
    const data = await response.json();
    return data.result;
  },

  async activateProblem(problemId: number, totalTestcases: number): Promise<AdminProblem> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/problems/${problemId}/activate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ totalTestcases }),
      credentials: 'include'
    });
    if (!response.ok) {
      throw new Error('Failed to activate problem');
    }
    const data = await response.json();
    return data.result;
  },

  async updateProblem(problemId: number, problem: Omit<AdminProblem, 'id' | 'createdAt' | 'createdBy' | 'isActive' | 'totalSubmissions' | 'acceptedSubmissions'>): Promise<AdminProblem> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/problems/${problemId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(problem),
      credentials: 'include'
    });
    if (!response.ok) {
      throw new Error('Failed to update problem');
    }
    const data = await response.json();
    return data.result;
  },

  async deleteProblem(problemId: number): Promise<void> {
    const response = await fetch(`${BASE_URL}/admin/problems/${problemId}`, {
      method: 'DELETE',
      credentials: 'include'
    });
    if (!response.ok) {
      throw new Error('Failed to delete problem');
    }
  },

  async getProblemVersions(problemId: number): Promise<any[]> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/problems/${problemId}/versions`, {
      method: 'GET',
      credentials: 'include'
    });
    if (!response.ok) {
      throw new Error('Failed to fetch problem versions');
    }
    const data = await response.json();
    return data.result;
  },

  async rollbackProblemVersion(problemId: number, versionId: number): Promise<any> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/problems/${problemId}/rollback/${versionId}`, {
      method: 'POST',
      credentials: 'include'
    });
    if (!response.ok) {
      throw new Error('Failed to rollback problem version');
    }
    const data = await response.json();
    return data.result;
  },

  async getTags(): Promise<{ id: number; name: string; slug: string }[]> {
    const response = await fetch(`${BASE_URL}/admin/problems/tags`, { credentials: 'include' });
    if (!response.ok) {
      throw new Error('Failed to fetch problem tags');
    }
    const data = await response.json();
    return data.result;
  },

  async getContestSubmissions(contestId: number): Promise<any[]> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/contests/${contestId}/submissions`, { credentials: 'include' });
    if (!response.ok) {
      throw new Error('Failed to fetch contest submissions');
    }
    const data = await response.json();
    return data.result || [];
  },

  async getContestScoreboard(contestId: number): Promise<any> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/api/v1/contests/${contestId}/scoreboard`, { credentials: 'include' });
    if (!response.ok) {
      throw new Error('Failed to fetch contest scoreboard');
    }
    const data = await response.json();
    return data.result;
  },

  async getCourseModerationReport(courseId: number | string): Promise<any> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/api/moderation/report/${courseId}`, { credentials: 'include' });
    if (!response.ok) {
      throw new Error('Failed to fetch AI moderation report');
    }
    return response.json();
  },

  async triggerAiModeration(courseId: number | string): Promise<any> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/api/moderation/test/${courseId}`, {
      method: 'POST',
      credentials: 'include'
    });
    if (!response.ok) {
      throw new Error('Failed to trigger AI moderation');
    }
    return response.json();
  },

  // Contests
  async getContests(): Promise<AdminContest[]> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/contests`, { credentials: 'include' });
      if (response.ok) {
        const data = await response.json();
        return data.result || [];
      }
    } catch (err) {
      console.warn("Failed to get Contests from backend:", err);
    }
    return [];
  },

  async createContest(contest: Omit<AdminContest, 'id' | 'status' | 'participantCount' | 'submissionCount' | 'averageScore'>): Promise<AdminContest> {
    const body = {
      ...contest,
      startTime: contest.startTime ? new Date(contest.startTime).toISOString() : undefined,
      endTime: contest.endTime ? new Date(contest.endTime).toISOString() : undefined,
    };
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/contests`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
      credentials: 'include'
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || 'Failed to create contest');
    }
    const data = await response.json();
    return data.result;
  },

  async getContestProblems(contestId: number): Promise<any[]> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/contests/${contestId}/problems`, { credentials: 'include' });
    if (!response.ok) {
      throw new Error('Failed to fetch contest problems');
    }
    const data = await response.json();
    return data.result || [];
  },

  async addProblemToContest(contestId: number, problemId: number, orderIndex: number): Promise<void> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/contests/${contestId}/problems`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ problemId, orderIndex }),
      credentials: 'include'
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || 'Failed to add problem to contest');
    }
  },

  async removeProblemFromContest(contestId: number, problemId: number): Promise<void> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/contests/${contestId}/problems/${problemId}`, {
      method: 'DELETE',
      credentials: 'include'
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || 'Failed to remove problem from contest');
    }
  },

  async updateContest(contestId: number, contest: Partial<AdminContest>): Promise<AdminContest> {
    const body = {
      ...contest,
      startTime: contest.startTime ? new Date(contest.startTime).toISOString() : undefined,
      endTime: contest.endTime ? new Date(contest.endTime).toISOString() : undefined,
    };
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/contests/${contestId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
      credentials: 'include'
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || 'Failed to update contest');
    }
    const data = await response.json();
    return data.result;
  },

  async deleteContest(contestId: number): Promise<void> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/contests/${contestId}`, {
      method: 'DELETE',
      credentials: 'include'
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || 'Failed to delete contest');
    }
  },

  async publishContest(contestId: number): Promise<AdminContest> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/contests/${contestId}/publish`, {
      method: 'PUT',
      credentials: 'include'
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || 'Failed to publish contest');
    }
    const data = await response.json();
    return data.result;
  },

  async restoreContest(contestId: number): Promise<AdminContest> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/contests/${contestId}/restore`, {
      method: 'PUT',
      credentials: 'include'
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || 'Failed to restore contest');
    }
    const data = await response.json();
    return data.result;
  },

  async hardDeleteContest(contestId: number): Promise<void> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/contests/${contestId}/hard`, {
      method: 'DELETE',
      credentials: 'include'
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || 'Failed to permanently delete contest');
    }
  },


  // Financial Chart details for 12 months
  getFinancialChartData() {
    return [
      { label: 'Jul 25', amount: 14000000, count: 28, usersCount: 18 },
      { label: 'Aug 25', amount: 16500000, count: 33, usersCount: 21 },
      { label: 'Sep 25', amount: 15000000, count: 30, usersCount: 25 },
      { label: 'Oct 25', amount: 17200000, count: 34, usersCount: 30 },
      { label: 'Nov 25', amount: 19000000, count: 38, usersCount: 29 },
      { label: 'Dec 25', amount: 21500000, count: 43, usersCount: 35 },
      { label: 'Jan 26', amount: 12000000, count: 24, usersCount: 15 },
      { label: 'Feb 26', amount: 15000000, count: 30, usersCount: 22 },
      { label: 'Mar 26', amount: 18500000, count: 37, usersCount: 31 },
      { label: 'Apr 26', amount: 16000000, count: 32, usersCount: 28 },
      { label: 'May 26', amount: 22000000, count: 44, usersCount: 45 },
      { label: 'Jun 26', amount: 24580000, count: 49, usersCount: 52 }
    ];
  },

  async getFinancialMonthlyRecords(): Promise<MonthlyFinancialRecord[]> {
    try {
      const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/financial/monthly-records`, { credentials: 'include' });
      if (response.ok) {
        const data = await response.json();
        return data.result;
      }
    } catch (err) {
      console.warn("Using mock data for Monthly Records:", err);
    }
    await delay(300);
    return [
      { label: 'Jul 25', datePrefix: '2025-07', gross: 14000000, count: 28, rewards: 800000, server: 1200000, marketing: 1000000 },
      { label: 'Aug 25', datePrefix: '2025-08', gross: 16500000, count: 33, rewards: 1000000, server: 1200000, marketing: 1200000 },
      { label: 'Sep 25', datePrefix: '2025-09', gross: 15000000, count: 30, rewards: 1200000, server: 1200000, marketing: 1000000 },
      { label: 'Oct 25', datePrefix: '2025-10', gross: 17200000, count: 34, rewards: 900000, server: 1200000, marketing: 1500000 },
      { label: 'Nov 25', datePrefix: '2025-11', gross: 19000000, count: 38, rewards: 1000000, server: 1500000, marketing: 1500000 },
      { label: 'Dec 25', datePrefix: '2025-12', gross: 21500000, count: 43, rewards: 1500000, server: 1500000, marketing: 2000000 },
      { label: 'Jan 26', datePrefix: '2026-01', gross: 12000000, count: 24, rewards: 800000, server: 1500000, marketing: 800000 },
      { label: 'Feb 26', datePrefix: '2026-02', gross: 15000000, count: 30, rewards: 1000000, server: 1500000, marketing: 1000000 },
      { label: 'Mar 26', datePrefix: '2026-03', gross: 18500000, count: 37, rewards: 1200000, server: 1500000, marketing: 1500000 },
      { label: 'Apr 26', datePrefix: '2026-04', gross: 16000000, count: 32, rewards: 1000000, server: 1500000, marketing: 1200000 },
      { label: 'May 26', datePrefix: '2026-05', gross: 22000000, count: 44, rewards: 1500000, server: 1500000, marketing: 1800000 },
      { label: 'Jun 26', datePrefix: '2026-06', gross: 24580000, count: 49, rewards: 1800000, server: 1500000, marketing: 2000000 }
    ];
  },

  async getFinancialTopCourses(): Promise<TopRevenueCourse[]> {
    try {
      const response = await fetch(`${BASE_URL}/admin/financial/top-courses`, { credentials: 'include' });
      if (response.ok) {
        const data = await response.json();
        return data.result;
      }
    } catch (err) {
      console.warn("Using mock data for Top Courses:", err);
    }
    await delay(300);
    return [
      { name: 'Mastering Full-Stack React & Node.js', tutor: 'Dr. Jenkins', sold: 340, gross: 169660000, payout: 118762000, plat: 50898000 },
      { name: 'Java Algorithms & Coding Arena', tutor: 'Alice Miller', sold: 210, gross: 81690000, payout: 57183000, plat: 24507000 },
      { name: 'Go Microservices & Dockerized Deployments', tutor: 'John Doe', sold: 80, gross: 52000000, payout: 36400000, plat: 15600000 },
      { name: 'Python Data Science and Machine Learning', tutor: 'Dr. Jenkins', sold: 50, gross: 29950000, payout: 20965000, plat: 8985000 }
    ];
  },

  async getFinancialDetails(): Promise<AdminFinancialDetails> {
    const response = await fetch(`${BASE_URL}/admin/financial/details`, { credentials: 'include' });
    if (!response.ok) {
      throw new Error('Failed to fetch financial audit details');
    }
    const data = await response.json();
    return data.result;
  },

  async getFinancialOrders(page: number, limit: number, startDate?: string, endDate?: string): Promise<PageResponse<OrderDetails>> {
    let url = `${BASE_URL}/admin/financial/orders?page=${page}&limit=${limit}`;
    if (startDate) url += `&startDate=${startDate}`;
    if (endDate) url += `&endDate=${endDate}`;
    const response = await fetch(url, { credentials: 'include' });
    if (!response.ok) {
      throw new Error('Failed to fetch financial orders');
    }
    const data = await response.json();
    return data.result;
  },

  async getFinancialAwards(page: number, limit: number, startDate?: string, endDate?: string): Promise<PageResponse<AwardDetails>> {
    let url = `${BASE_URL}/admin/financial/awards?page=${page}&limit=${limit}`;
    if (startDate) url += `&startDate=${startDate}`;
    if (endDate) url += `&endDate=${endDate}`;
    const response = await fetch(url, { credentials: 'include' });
    if (!response.ok) {
      throw new Error('Failed to fetch financial awards');
    }
    const data = await response.json();
    return data.result;
  },

  async getFinancialSales(page: number, limit: number, startDate?: string, endDate?: string): Promise<PageResponse<SaleDetails>> {
    let url = `${BASE_URL}/admin/financial/sales?page=${page}&limit=${limit}`;
    if (startDate) url += `&startDate=${startDate}`;
    if (endDate) url += `&endDate=${endDate}`;
    const response = await fetch(url, { credentials: 'include' });
    if (!response.ok) {
      throw new Error('Failed to fetch financial sales');
    }
    const data = await response.json();
    return data.result;
  },

  async getFinancialPayouts(page: number = 1, size: number = 10, startDate?: string, endDate?: string): Promise<PageResponse<PayoutDetails>> {
    let url = `${BASE_URL}/admin/financial/payouts?page=${page}&limit=${size}`;
    if (startDate) url += `&startDate=${startDate}`;
    if (endDate) url += `&endDate=${endDate}`;
    const response = await fetch(url, { credentials: 'include' });
    if (!response.ok) {
      throw new Error('Failed to fetch financial payouts');
    }
    const data = await response.json();
    return data.result;
  },

  async getProblemTestcases(problemId: number): Promise<AdminProblemTestcase[]> {
    const response = await fetch(`${BASE_URL}/admin/problems/${problemId}/testcases`, { credentials: 'include' });
    if (!response.ok) {
      throw new Error('Failed to load test cases from database');
    }
    const data = await response.json();
    return data.result || [];
  },

  async saveProblemTestcases(problemId: number, testcases: Omit<AdminProblemTestcase, 'id'>[]): Promise<AdminProblemTestcase[]> {
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/problems/${problemId}/testcases`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(testcases),
      credentials: 'include'
    });
    if (!response.ok) {
      throw new Error('Failed to save test cases to database');
    }
    const data = await response.json();
    await this.activateProblem(problemId, data.result.length);
    return data.result;
  },

  async uploadTestcaseZip(problemId: number, file: File): Promise<AdminProblemTestcase[]> {
    const formData = new FormData();
    formData.append('file', file);
    const response = await fetchWithAutoRefresh(`${BASE_URL}/admin/problems/${problemId}/testcases/upload`, {
      method: 'POST',
      body: formData,
      credentials: 'include'
    });
    if (!response.ok) {
      throw new Error('Failed to upload ZIP archive to database');
    }
    const data = await response.json();
    await this.activateProblem(problemId, data.result.length);
    return data.result;
  }
};
