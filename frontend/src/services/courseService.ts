export interface CourseChapter {
  id: string;
  title: string;
  duration: string;
  lessons: { id: string; title: string; duration: string; type: 'video' | 'coding' | 'reading' }[];
}

export interface Course {
  id: string;
  title: string;
  category: 'Algorithms' | 'Core Knowledge' | 'Basic Programming' | 'Advanced Programming' | 'Problem Solving' | 'Advanced Skills';
  author: string;
  authorTitle: string;
  authorAvatar: string;
  authorBio: string;
  rating: number;
  enrolled: number;
  price: number;
  originalPrice?: number;
  level: 'beginner' | 'intermediate' | 'advanced';
  languages: string[]; // e.g. ["python", "cpp", "java", "javascript", "go"]
  releaseDate: string;
  thumbnailUrl: string;
  iconName: string;
  description: string;
  syllabus: CourseChapter[];
  whatYouWillLearn: string[];
}

export const mockCourses: Course[] = [
  {
    id: 'c1',
    title: 'Introduction to Python Programming Fundamentals',
    category: 'Basic Programming',
    author: 'Dr. Alan Turing',
    authorTitle: 'Professor of Computer Science',
    authorAvatar: 'https://ui-avatars.com/api/?name=Alan+Turing&background=12284C&color=fff',
    authorBio: 'Dr. Turing is a pioneer in computer science education, specializing in fundamental programming language concepts and computational theory.',
    rating: 4.8,
    enrolled: 1240,
    price: 1250000,
    originalPrice: 2250000,
    level: 'beginner',
    languages: ['python'],
    releaseDate: '2026-05-10',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBgy50UMGsrfiNlaMOGS5hIFfEB9ALLj2hHwL19FjiPxHtPdmdzDshyKCd9cxUE55L1IPGibJJ8XxYWvIOtq6nCmPgaCFoPxxlN64_OwyPrZocxC4bEzFtpL_km1YmpuA-CN4fUVjD5gO2NI7mdCoim7_CAT7njSdYphWceJpEIiRp5PAaZrqeglhZ4z73HAhMVJI5rSTTAUK3BmjBzHCR2ivCNvmKAvTRSv0bZDvGjfSB2GENwq1duU8S0jsS3Bgtxt-P5YEUi6M8',
    iconName: 'terminal',
    description: 'Learn the basic syntax and structures of Python, starting from simple expressions up to object-oriented constructs, file handling, and algorithms.',
    whatYouWillLearn: [
      'Hiểu và áp dụng các cú pháp cơ bản của Python',
      'Xử lý cấu trúc rẽ nhánh, vòng lặp và hàm',
      'Làm quen với Lập trình hướng đối tượng cơ bản',
      'Đọc, ghi file và phân tích dữ liệu dạng bảng với Pandas'
    ],
    syllabus: [
      {
        id: 'ch1',
        title: 'Chương 1: Mở đầu với Python',
        duration: '1.5 hours',
        lessons: [
          { id: 'l1', title: 'Bài 1: Giới thiệu khóa học và Cài đặt môi trường', duration: '15 mins', type: 'video' },
          { id: 'l2', title: 'Bài 2: Variables & Basic Data types', duration: '20 mins', type: 'video' },
          { id: 'l3', title: 'Bài tập: Phép toán cơ bản', duration: '15 mins', type: 'coding' }
        ]
      }
    ]
  },
  {
    id: 'c2',
    title: 'Data Structures and Algorithms Mastery',
    category: 'Algorithms',
    author: 'Ada Lovelace',
    authorTitle: 'Senior Algorithm Architect',
    authorAvatar: 'https://ui-avatars.com/api/?name=Ada+Lovelace&background=46A040&color=fff',
    authorBio: 'Ada Lovelace is a veteran engineer with 15+ years of experience leading core math and data processing teams at global enterprise corporations.',
    rating: 4.9,
    enrolled: 856,
    price: 1999000,
    originalPrice: 3000000,
    level: 'advanced',
    languages: ['cpp', 'java'],
    releaseDate: '2026-04-20',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBrLRPGmTw2WKOVjaU8vt3rWbyU_IutkyQCHmjb4756OHz94BzCcaaqOAypjovZ890SBIthYzF12ggMvhxo0w-S_OQizNFa5DtyTQfi3KxxxubXCobRHCPMK2auxCeFzRISNcp72GUb3AXRG4IbJSc1j1jqMRfbhbXBZFOzuEs9Zyv3mgRrXDRBAujfgQw5_uGSeKQI340ZtVWM81ZNu887j7-Ee2CMIXLXPiIRuva9t7_xMz7YydCPH56sKDASIrKT-SFU_pzI-q0',
    iconName: 'data_object',
    description: 'Master core algorithmic paradigms (dynamic programming, greedy, graphs) and foundational data structures (trees, heaps, queues) for competitive coding.',
    whatYouWillLearn: [
      'Thiết kế và tối ưu hóa cấu trúc dữ liệu tùy chỉnh',
      'Sử dụng thuật toán Tìm kiếm, Sắp xếp nâng cao',
      'Nắm vững Quy hoạch động, Đồ thị và Tham lam',
      'Đủ khả năng vượt qua vòng phỏng vấn kỹ thuật của BigTech'
    ],
    syllabus: []
  },
  {
    id: 'c3',
    title: 'Machine Learning Foundations for Developers',
    category: 'Core Knowledge',
    author: 'Grace Hopper',
    authorTitle: 'AI Research Director',
    authorAvatar: 'https://ui-avatars.com/api/?name=Grace+Hopper&background=F36F21&color=fff',
    authorBio: 'Grace has researched deep learning structures and mathematical foundations at global technology institutes for over a decade.',
    rating: 4.7,
    enrolled: 3102,
    price: 1500000,
    level: 'intermediate',
    languages: ['python'],
    releaseDate: '2026-05-15',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDprSKVqEq347pPqZ9M8ZWp_6T-Pvi_68sA90ExU-mSJXsImRMFa4q4dLHkArN6WOv5WFywpvaSZBRAHvu_Dx0r6w9yK_mlTECqCeq9Wg3oBbgZTv9n5f5XBS7cYcelKHCSqcutDcmpUqgS0-UThBEEYGjKVVlqjNkMD5LeFuWllGb4uhmZZ8l2nvSElcuet9dv6J2P59fo1VSbODozVKEkm5a4gpdTPT1T6CEHtGUDY7Lv6jRnLSmwUI2aNOpki1r5UtOOo4ccDQQ',
    iconName: 'analytics',
    description: 'Learn the practical application of machine learning pipelines, linear regression, classification models, and neural networks using Python libraries.',
    whatYouWillLearn: [
      'Xây dựng các mô hình hồi quy tuyến tính và phân lớp',
      'Tiền xử lý và làm sạch dữ liệu lớn thực tế',
      'Ứng dụng Scikit-Learn, NumPy, Pandas chuyên nghiệp',
      'Hiểu bản chất toán học của các thuật toán ML chính'
    ],
    syllabus: []
  },
  {
    id: 'c4',
    title: 'Advanced Cloud Architecture & Deployment',
    category: 'Advanced Programming',
    author: 'Linus Torvalds',
    authorTitle: 'Lead Cloud Infrastructure Architect',
    authorAvatar: 'https://ui-avatars.com/api/?name=Linus+Torvalds&background=12284C&color=fff',
    authorBio: 'Linus Torvalds is an open-source advocate and chief architect behind some of the world\'s largest cloud virtualization systems.',
    rating: 4.9,
    enrolled: 412,
    price: 0,
    originalPrice: 1999000,
    level: 'advanced',
    languages: ['go'],
    releaseDate: '2026-05-20',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAgPQoJfVaRjxXQvS7N25MLctJghBgjcZCo8n2wpzkZMyEuTKFIvYs8qJ2OMD4PEp3G9tRzvqizo-W5TB-OIXup91n-sqoxw6_rFv5ZF7yMaaV5SWkkzoIX9SKxkU7xITu5AyPYUDImqxHExSi0alwlwCBoyyJ7vCnwTnwGJDlY9rskNWGjxxW-zx-A3-RRo_W1zlMWhLftwYj33PdKOQgv3aJAGj69mGWeFoSUXXRlcY-kkal5mjfr19Uf3qELIcDhvG1oiKO4s90',
    iconName: 'dns',
    description: 'Master microservice orchestration, Kubernetes clusters, Docker configurations, and secure cloud networking using Go.',
    whatYouWillLearn: [
      'Thiết lập kiến trúc Microservices hiệu năng cao',
      'Triển khai container hóa với Docker và Kubernetes',
      'Cấu hình mạng đám mây an toàn, chịu lỗi tốt',
      'CI/CD Pipelines tự động trên môi trường AWS/GCP'
    ],
    syllabus: []
  },
  {
    id: 'c5',
    title: 'Web Development Bootcamp 2026',
    category: 'Basic Programming',
    author: 'Tim Berners-Lee',
    authorTitle: 'W3C Standard Fellow',
    authorAvatar: 'https://ui-avatars.com/api/?name=Tim+Berners-Lee&background=46A040&color=fff',
    authorBio: 'Tim has dedicated his life to standardizing front-end specifications, teaching thousands of learners the core technologies powering the modern web.',
    rating: 4.6,
    enrolled: 8901,
    price: 750000,
    originalPrice: 1250000,
    level: 'beginner',
    languages: ['javascript'],
    releaseDate: '2026-03-10',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBJyORKWLp748TXseYyDhN53kvX6pZ4KFcscGLQ7cUxMIn7tMLeYeeOMPkwRBYN-DtN_I-uagbYLxNqiPVzeeRSHN_eEpbOKKCuipy6kriMB5t_3x17QVJWb9Vtud7BfdexPZ7C1Lr3hBjWiLj7uPb3xeWiSiWQVa9eSiawc4i9NDjvBttSrqSqFMZYnShy86b3VS-BDUs3zdMFUNviGGwyXK_YoQtPna6HDktIc31wWKH597aPjX_fAB2iQQLZ_dmqzevaMhf4YSA',
    iconName: 'code',
    description: 'Learn dynamic HTML5 layout design, CSS3 typography and layout architectures, and vanilla JS web interaction features.',
    whatYouWillLearn: [
      'Sử dụng thành thạo HTML5, CSS3 và JavaScript ES6+',
      'Thiết kế giao diện thích ứng (Responsive Design)',
      'Lập trình điều khiển DOM và bắt sự kiện tương tác',
      'Đưa trang web lên máy chủ deploy toàn cầu'
    ],
    syllabus: []
  },
  {
    id: 'c6',
    title: 'Agile Methodologies & Team Collaboration',
    category: 'Problem Solving',
    author: 'Margaret Hamilton',
    authorTitle: 'Agile Coach & Systems Engineer',
    authorAvatar: 'https://ui-avatars.com/api/?name=Margaret+Hamilton&background=12284C&color=fff',
    authorBio: 'Margaret is a pioneer in systems engineering and software methodologies, helping teams implement Agile practices at scale.',
    rating: 4.8,
    enrolled: 620,
    price: 1100000,
    level: 'intermediate',
    languages: ['javascript'],
    releaseDate: '2026-02-15',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDdvixWa0Ipwk8rpkKX9qeh70ObUfCXLKvpj-YiwNray1_KAAFszoBui24Ha5IX5zIpJViO099pDhWAxT3dKXpJdRmfuy4ZW6iQN8BLm3frArTM5XU7TUWvyOCkvSGLA9AfugrWrTIyT17MWXQcti61jfHeYi_WDq9GzmiekKYQP1qNFPWTGo6eeaAUGx9CSffdxfhGqEwBshZz4CW0PiHg8Qf1eQI8hvcRX48BR59xCF--PVVN2CKbczyFRlaxcKcCsd-994gWFnc',
    iconName: 'group',
    description: 'Learn dynamic Agile structures, SCRUM practices, and software development coordination paradigms for modern team success.',
    whatYouWillLearn: [
      'Hiểu quy trình Scrum và Kanban',
      'Tổ chức sprint planning hiệu quả',
      'Cải thiện kỹ năng giao tiếp nhóm',
      'Sử dụng Jira/Trello chuyên nghiệp'
    ],
    syllabus: []
  },
  {
    id: 'c7',
    title: 'FinTech Development: Smart Contracts',
    category: 'Advanced Skills',
    author: 'Satoshi Nakamoto',
    authorTitle: 'Cryptographic Engineer & Architect',
    authorAvatar: 'https://ui-avatars.com/api/?name=Satoshi+Nakamoto&background=46A040&color=fff',
    authorBio: 'Satoshi Nakamoto is a developer and cryptographer specializing in decentralized ledgers and distributed systems design.',
    rating: 4.5,
    enrolled: 210,
    price: 1800000,
    level: 'advanced',
    languages: ['go'],
    releaseDate: '2026-05-01',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBqOW4V5OECgEQbRBZGKszuwTRmVQehGYR4VmXg1GDgXGvD1tScs_9-GyOm4Uh6dY3iI8qcp_JQYpjKoBQvJ7hiCTq9yJ8Cv3H1eFe00po6Q-SsOad0w4z74ePecTgvS6Lk2U9O58RFwc4O2gVjGgh46HY-S3uKiLLg2hzzfp5naSkcjJC6Hj8I3oGfahGUgD39lCpuCYT_5AIuOOqf92damSBj0vJmtCTCRO_Rt-4ptpOfb2vPRWEOPGHb0NkqEGgP1QtjIURA7PY',
    iconName: 'payments',
    description: 'Design cryptographic transaction platforms, blockchain ledgers, and secure decentralized financial smart contracts with Go.',
    whatYouWillLearn: [
      'Hiểu kiến trúc Blockchain & Smart Contracts',
      'Lập trình Smart Contract hiệu năng cao bằng Go',
      'Bảo mật FinTech & Phòng chống lỗ hổng',
      'Tích hợp ví điện tử và cổng thanh toán phi tập trung'
    ],
    syllabus: []
  },
  {
    id: 'c8',
    title: 'Cybersecurity Basics for Software Engineers',
    category: 'Core Knowledge',
    author: 'Kevin Mitnick',
    authorTitle: 'Global Security Consultant',
    authorAvatar: 'https://ui-avatars.com/api/?name=Kevin+Mitnick&background=F36F21&color=fff',
    authorBio: 'Kevin Mitnick is a security author and legendary investigator of computer system vulnerabilities and defensive structures.',
    rating: 4.9,
    enrolled: 1105,
    price: 2000000,
    originalPrice: 2500000,
    level: 'beginner',
    languages: ['java', 'python'],
    releaseDate: '2026-05-24',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBcrPGMmE7CAW8JZJze5J_dyu8A8hvGF7QHl6NxROMszM_yggVTLZRnh3mqeFZ27qGR0S2JCtbRu-GItuuqB6hE7mNjgAOl4PVtCkftcjREaSsMx1f-KhQ49edArmcJ-bI0TZneA0ZxAMla7ZOYmHPyOmyA6RhKwRRV1vgyzUQecPXXfdoqEuJqWHc33pPMJ4bz0otgj6TnrvJoDn9fEcpAEZ5MpisoRZaDnGMkWP5GakK53buxa-aU0ycd7PZXOZTD0oHgNiCvfvQ',
    iconName: 'lock',
    description: 'Learn to design secure programming layers, encrypt transmission packages, and prevent SQL injection or cross-site scripting vulnerabilities.',
    whatYouWillLearn: [
      'Xác định và khắc phục Top 10 OWASP',
      'Mã hóa dữ liệu & Quản lý khóa an toàn',
      'Thiết lập tường lửa & Phát hiện xâm nhập',
      'Viết mã nguồn an toàn chống khai thác lỗ hổng'
    ],
    syllabus: []
  },
  {
    id: 'c9',
    title: 'Enterprise Java Development with Spring Boot',
    category: 'Advanced Programming',
    author: 'James Gosling',
    authorTitle: 'Java Language Inventor',
    authorAvatar: 'https://ui-avatars.com/api/?name=James+Gosling&background=12284C&color=fff',
    authorBio: 'James is the legendary creator of the Java programming language and an expert in high-scalability multi-threaded systems.',
    rating: 4.7,
    enrolled: 1540,
    price: 1600000,
    originalPrice: 2400000,
    level: 'intermediate',
    languages: ['java'],
    releaseDate: '2026-05-18',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBrLRPGmTw2WKOVjaU8vt3rWbyU_IutkyQCHmjb4756OHz94BzCcaaqOAypjovZ890SBIthYzF12ggMvhxo0w-S_OQizNFa5DtyTQfi3KxxxubXCobRHCPMK2auxCeFzRISNcp72GUb3AXRG4IbJSc1j1jqMRfbhbXBZFOzuEs9Zyv3mgRrXDRBAujfgQw5_uGSeKQI340ZtVWM81ZNu887j7-Ee2CMIXLXPiIRuva9t7_xMz7YydCPH56sKDASIrKT-SFU_pzI-q0',
    iconName: 'settings_input_component',
    description: 'Build scalable back-end API architectures, deploy microservice models, and construct automated unit-test suites using Spring Boot and Hibernate.',
    whatYouWillLearn: [
      'Xây dựng REST APIs chuẩn doanh nghiệp',
      'Sử dụng Spring Data JPA kết nối cơ sở dữ liệu',
      'Bảo mật hệ thống với Spring Security & JWT',
      'Triển khai ứng dụng Java lên nền tảng đám mây'
    ],
    syllabus: []
  },
  {
    id: 'c10',
    title: 'Competitive Programming in C++: Core Strategies',
    category: 'Algorithms',
    author: 'Bjarne Stroustrup',
    authorTitle: 'C++ Language Creator',
    authorAvatar: 'https://ui-avatars.com/api/?name=Bjarne+Stroustrup&background=46A040&color=fff',
    authorBio: 'Bjarne designed C++ to solve complex hardware-level virtualization, and teaches top-performance mathematical algorithms and optimizations.',
    rating: 4.9,
    enrolled: 2350,
    price: 1200000,
    level: 'advanced',
    languages: ['cpp'],
    releaseDate: '2026-05-22',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBgy50UMGsrfiNlaMOGS5hIFfEB9ALLj2hHwL19FjiPxHtPdmdzDshyKCd9cxUE55L1IPGibJJ8XxYWvIOtq6nCmPgaCFoPxxlN64_OwyPrZocxC4bEzFtpL_km1YmpuA-CN4fUVjD5gO2NI7mdCoim7_CAT7njSdYphWceJpEIiRp5PAaZrqeglhZ4z73HAhMVJI5rSTTAUK3BmjBzHCR2ivCNvmKAvTRSv0bZDvGjfSB2GENwq1duU8S0jsS3Bgtxt-P5YEUi6M8',
    iconName: 'military_tech',
    description: 'Learn core template configurations, custom performance metrics, bit manipulation tactics, and extreme graph representations in competitive environments.',
    whatYouWillLearn: [
      'Tối ưu hóa thời gian thực thi mã C++',
      'Sử dụng cấu trúc dữ liệu nâng cao như Fenwick Tree, Segment Tree',
      'Giải các bài toán quy hoạch động phức tạp trên LeetCode/Codeforces',
      'Kỹ thuật tối ưu bit và toán học tổ hợp'
    ],
    syllabus: []
  },
  {
    id: 'c11',
    title: 'Python for Data Science and Data Analytics',
    category: 'Core Knowledge',
    author: 'Guido van Rossum',
    authorTitle: 'Creator of Python',
    authorAvatar: 'https://ui-avatars.com/api/?name=Guido+van+Rossum&background=F36F21&color=fff',
    authorBio: 'Guido designed Python for optimal developer readability, making it the premier language for modern scientific computation.',
    rating: 4.8,
    enrolled: 4120,
    price: 850000,
    originalPrice: 1500000,
    level: 'beginner',
    languages: ['python'],
    releaseDate: '2026-04-15',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDprSKVqEq347pPqZ9M8ZWp_6T-Pvi_68sA90ExU-mSJXsImRMFa4q4dLHkArN6WOv5WFywpvaSZBRAHvu_Dx0r6w9yK_mlTECqCeq9Wg3oBbgZTv9n5f5XBS7cYcelKHCSqcutDcmpUqgS0-UThBEEYGjKVVlqjNkMD5LeFuWllGb4uhmZZ8l2nvSElcuet9dv6J2P59fo1VSbODozVKEkm5a4gpdTPT1T6CEHtGUDY7Lv6jRnLSmwUI2aNOpki1r5UtOOo4ccDQQ',
    iconName: 'query_stats',
    description: 'Analyze large-scale tabular frames, generate sophisticated chart visualizations, and process numerical computations using Pandas and NumPy.',
    whatYouWillLearn: [
      'Thu thập và tiền xử lý dữ liệu với Pandas',
      'Trực quan hóa dữ liệu bằng Matplotlib và Seaborn',
      'Phân tích thống kê và kiểm định giả thuyết cơ bản',
      'Xử lý dữ liệu lớn trên Jupyter Notebook'
    ],
    syllabus: []
  },
  {
    id: 'c12',
    title: 'React & Next.js Advanced Architecture',
    category: 'Advanced Programming',
    author: 'Dan Abramov',
    authorTitle: 'Redux Co-creator & React Core Alum',
    authorAvatar: 'https://ui-avatars.com/api/?name=Dan+Abramov&background=12284C&color=fff',
    authorBio: 'Dan has spent years developing UI abstractions, defining dynamic state management paradigms used by millions of web developers.',
    rating: 4.9,
    enrolled: 3890,
    price: 1450000,
    originalPrice: 2200000,
    level: 'advanced',
    languages: ['javascript'],
    releaseDate: '2026-05-05',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBJyORKWLp748TXseYyDhN53kvX6pZ4KFcscGLQ7cUxMIn7tMLeYeeOMPkwRBYN-DtN_I-uagbYLxNqiPVzeeRSHN_eEpbOKKCuipy6kriMB5t_3x17QVJWb9Vtud7BfdexPZ7C1Lr3hBjWiLj7uPb3xeWiSiWQVa9eSiawc4i9NDjvBttSrqSqFMZYnShy86b3VS-BDUs3zdMFUNviGGwyXK_YoQtPna6HDktIc31wWKH597aPjX_fAB2iQQLZ_dmqzevaMhf4YSA',
    iconName: 'layers',
    description: 'Build modern web platforms using React Server Components, server-side caching mechanics, and Next.js App Router state topologies.',
    whatYouWillLearn: [
      'Làm chủ React Server Components (RSC)',
      'Cấu hình Next.js App Router nâng cao',
      'Quản lý State và Cache phía Server tối ưu',
      'Deploy Web App tối ưu điểm SEO và Core Web Vitals'
    ],
    syllabus: []
  },
  {
    id: 'c13',
    title: 'Microservices with Go & gRPC Fundamentals',
    category: 'Advanced Skills',
    author: 'Rob Pike',
    authorTitle: 'Go Language Co-creator',
    authorAvatar: 'https://ui-avatars.com/api/?name=Rob+Pike&background=46A040&color=fff',
    authorBio: 'Rob Pike co-designed the Go compiler at Google and is an expert in system concurrency, networking protocols, and cloud architecture.',
    rating: 4.6,
    enrolled: 980,
    price: 1150000,
    level: 'intermediate',
    languages: ['go'],
    releaseDate: '2026-03-25',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAgPQoJfVaRjxXQvS7N25MLctJghBgjcZCo8n2wpzkZMyEuTKFIvYs8qJ2OMD4PEp3G9tRzvqizo-W5TB-OIXup91n-sqoxw6_rFv5ZF7yMaaV5SWkkzoIX9SKxkU7xITu5AyPYUDImqxHExSi0alwlwCBoyyJ7vCnwTnwGJDlY9rskNWGjxxW-zx-A3-RRo_W1zlMWhLftwYj33PdKOQgv3aJAGj69mGWeFoSUXXRlcY-kkal5mjfr19Uf3qELIcDhvG1oiKO4s90',
    iconName: 'hub',
    description: 'Design fast RPC networks, handle asynchronous message payloads, and organize concurrent channel pipes in distributed environments using Go.',
    whatYouWillLearn: [
      'Xây dựng Microservices hiệu năng cao với Go',
      'Sử dụng Protocol Buffers & gRPC thay thế REST APIs',
      'Quản lý giao dịch phân tán và đồng bộ dữ liệu',
      'Thiết lập Service Discovery và API Gateway'
    ],
    syllabus: []
  },
  {
    id: 'c14',
    title: 'Introduction to Dynamic Programming',
    category: 'Algorithms',
    author: 'Richard Bellman',
    authorTitle: 'Applied Mathematics Pioneer',
    authorAvatar: 'https://ui-avatars.com/api/?name=Richard+Bellman&background=F36F21&color=fff',
    authorBio: 'Richard Bellman introduced dynamic programming concepts, showing developers how to simplify complex decision processes through subproblem analysis.',
    rating: 4.8,
    enrolled: 612,
    price: 0,
    originalPrice: 1250000,
    level: 'intermediate',
    languages: ['java', 'python'],
    releaseDate: '2026-05-12',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBrLRPGmTw2WKOVjaU8vt3rWbyU_IutkyQCHmjb4756OHz94BzCcaaqOAypjovZ890SBIthYzF12ggMvhxo0w-S_OQizNFa5DtyTQfi3KxxxubXCobRHCPMK2auxCeFzRISNcp72GUb3AXRG4IbJSc1j1jqMRfbhbXBZFOzuEs9Zyv3mgRrXDRBAujfgQw5_uGSeKQI340ZtVWM81ZNu887j7-Ee2CMIXLXPiIRuva9t7_xMz7YydCPH56sKDASIrKT-SFU_pzI-q0',
    iconName: 'dynamic_form',
    description: 'Learn overlapping subproblem optimizations, memoization strategies, tabulation structures, and top-down vs bottom-up problem strategies.',
    whatYouWillLearn: [
      'Nhận diện các bài toán giải bằng Quy hoạch động',
      'Thiết kế mảng Tabulation và hàm đệ quy có Memoization',
      'Tối ưu hóa độ phức tạp thời gian từ O(2^n) xuống O(n)',
      'Thực hành giải các bài toán kinh điển như Balo, Dãy con tăng dài nhất'
    ],
    syllabus: []
  },
  {
    id: 'c15',
    title: 'Object Oriented Programming in C++',
    category: 'Basic Programming',
    author: 'Scott Meyers',
    authorTitle: 'Distinguished C++ Specialist',
    authorAvatar: 'https://ui-avatars.com/api/?name=Scott+Meyers&background=12284C&color=fff',
    authorBio: 'Scott Meyers wrote bestselling guides on effective design patterns, specializing in optimization and resource management in complex C++ projects.',
    rating: 4.5,
    enrolled: 1420,
    price: 650000,
    originalPrice: 1100000,
    level: 'beginner',
    languages: ['cpp'],
    releaseDate: '2026-01-10',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBgy50UMGsrfiNlaMOGS5hIFfEB9ALLj2hHwL19FjiPxHtPdmdzDshyKCd9cxUE55L1IPGibJJ8XxYWvIOtq6nCmPgaCFoPxxlN64_OwyPrZocxC4bEzFtpL_km1YmpuA-CN4fUVjD5gO2NI7mdCoim7_CAT7njSdYphWceJpEIiRp5PAaZrqeglhZ4z73HAhMVJI5rSTTAUK3BmjBzHCR2ivCNvmKAvTRSv0bZDvGjfSB2GENwq1duU8S0jsS3Bgtxt-P5YEUi6M8',
    iconName: 'deployed_code',
    description: 'Construct solid encapsulation frameworks, master inheritances and polymorphism layers, and handle manual memory allocation utilizing pointers safely.',
    whatYouWillLearn: [
      'Áp dụng 4 tính chất OOP trong C++',
      'Quản lý bộ nhớ thủ công với Pointers và Smart Pointers',
      'Sử dụng Class Template và Function Template',
      'Phân tích và triển khai các Design Patterns cơ bản'
    ],
    syllabus: []
  },
  {
    id: 'c16',
    title: 'Building REST APIs with FastAPI and Python',
    category: 'Basic Programming',
    author: 'Sebastián Ramírez',
    authorTitle: 'FastAPI Creator & Lead Developer',
    authorAvatar: 'https://ui-avatars.com/api/?name=Sebastian+Ramirez&background=46A040&color=fff',
    authorBio: 'Sebastián designed FastAPI to leverage modern Python type hints, delivering highly performant API networks that build documentation automatically.',
    rating: 4.9,
    enrolled: 2840,
    price: 950000,
    level: 'beginner',
    languages: ['python'],
    releaseDate: '2026-05-23',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDprSKVqEq347pPqZ9M8ZWp_6T-Pvi_68sA90ExU-mSJXsImRMFa4q4dLHkArN6WOv5WFywpvaSZBRAHvu_Dx0r6w9yK_mlTECqCeq9Wg3oBbgZTv9n5f5XBS7cYcelKHCSqcutDcmpUqgS0-UThBEEYGjKVVlqjNkMD5LeFuWllGb4uhmZZ8l2nvSElcuet9dv6J2P59fo1VSbODozVKEkm5a4gpdTPT1T6CEHtGUDY7Lv6jRnLSmwUI2aNOpki1r5UtOOo4ccDQQ',
    iconName: 'api',
    description: 'Design fast JSON server applications, integrate clean relational database models, and apply JWT authentication security routines in Python.',
    whatYouWillLearn: [
      'Xây dựng RESTful API chuẩn hóa, tự động sinh tài liệu Swagger/Redoc',
      'Sử dụng Pydantic để validate dữ liệu đầu vào chặt chẽ',
      'Tích hợp SQL Database thông qua SQLAlchemy/SQLModel',
      'Cấu hình Asynchronous (async/await) để xử lý lượng tải lớn'
    ],
    syllabus: []
  },
  {
    id: 'c17',
    title: 'Go Programming Language Core Fundamentals',
    category: 'Basic Programming',
    author: 'Ken Thompson',
    authorTitle: 'Legendary Computing Pioneer',
    authorAvatar: 'https://ui-avatars.com/api/?name=Ken+Thompson&background=F36F21&color=fff',
    authorBio: 'Ken co-designed Go and UNIX systems, specializing in developing simple compiler toolchains and highly optimize backend languages.',
    rating: 4.7,
    enrolled: 1250,
    price: 700000,
    originalPrice: 1200000,
    level: 'beginner',
    languages: ['go'],
    releaseDate: '2026-05-02',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAgPQoJfVaRjxXQvS7N25MLctJghBgjcZCo8n2wpzkZMyEuTKFIvYs8qJ2OMD4PEp3G9tRzvqizo-W5TB-OIXup91n-sqoxw6_rFv5ZF7yMaaV5SWkkzoIX9SKxkU7xITu5AyPYUDImqxHExSi0alwlwCBoyyJ7vCnwTnwGJDlY9rskNWGjxxW-zx-A3-RRo_W1zlMWhLftwYj33PdKOQgv3aJAGj69mGWeFoSUXXRlcY-kkal5mjfr19Uf3qELIcDhvG1oiKO4s90',
    iconName: 'code_blocks',
    description: 'Learn primary Go syntax patterns, declare struct blueprints, organize modules, and run concurrent functions via channels and goroutines safely.',
    whatYouWillLearn: [
      'Sử dụng thành thạo cú pháp cơ bản và kiểu dữ liệu của Go',
      'Làm quen với struct, interface và mô hình hướng đối tượng của Go',
      'Lập trình đa luồng cơ bản với Goroutine và Channel',
      'Xây dựng ứng dụng CLI và Web Server đơn giản'
    ],
    syllabus: []
  },
  {
    id: 'c18',
    title: 'TypeScript Mastery for Modern JavaScript Developers',
    category: 'Advanced Skills',
    author: 'Anders Hejlsberg',
    authorTitle: 'TypeScript Creator & Chief Architect',
    authorAvatar: 'https://ui-avatars.com/api/?name=Anders+Hejlsberg&background=12284C&color=fff',
    authorBio: 'Anders designed C# and TypeScript to improve large-scale enterprise development structures, helping millions write type-safe frontends.',
    rating: 4.8,
    enrolled: 1920,
    price: 1300000,
    originalPrice: 1950000,
    level: 'intermediate',
    languages: ['javascript'],
    releaseDate: '2026-05-16',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDdvixWa0Ipwk8rpkKX9qeh70ObUfCXLKvpj-YiwNray1_KAAFszoBui24Ha5IX5zIpJViO099pDhWAxT3dKXpJdRmfuy4ZW6iQN8BLm3frArTM5XU7TUWvyOCkvSGLA9AfugrWrTIyT17MWXQcti61jfHeYi_WDq9GzmiekKYQP1qNFPWTGo6eeaAUGx9CSffdxfhGqEwBshZz4CW0PiHg8Qf1eQI8hvcRX48BR59xCF--PVVN2CKbczyFRlaxcKcCsd-994gWFnc',
    iconName: 'verified_user',
    description: 'Write precise type definitions, configure complex decorators, implement generics parameters, and compile dynamic systems safely with TypeScript.',
    whatYouWillLearn: [
      'Hiểu và khai báo kiểu dữ liệu phức tạp (Generics, Union, Intersection)',
      'Sử dụng Utility Types tối ưu hóa code',
      'Cấu hình tsconfig.json cho dự án lớn',
      'Lập trình TypeScript tích hợp với React và Webpack'
    ],
    syllabus: []
  },
  {
    id: 'c19',
    title: 'LeetCode Patterns & Algorithmic Problem Solving',
    category: 'Problem Solving',
    author: 'Grokking The Code',
    authorTitle: 'Algorithm Coach & Educator',
    authorAvatar: 'https://ui-avatars.com/api/?name=Grokking+The+Code&background=46A040&color=fff',
    authorBio: 'Grokking has prepared thousands of learners for rigorous algorithmic interviews at top global organizations through pattern recognition.',
    rating: 4.9,
    enrolled: 5420,
    price: 1750000,
    originalPrice: 2500000,
    level: 'advanced',
    languages: ['cpp', 'python', 'java'],
    releaseDate: '2026-05-24',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBgy50UMGsrfiNlaMOGS5hIFfEB9ALLj2hHwL19FjiPxHtPdmdzDshyKCd9cxUE55L1IPGibJJ8XxYWvIOtq6nCmPgaCFoPxxlN64_OwyPrZocxC4bEzFtpL_km1YmpuA-CN4fUVjD5gO2NI7mdCoim7_CAT7njSdYphWceJpEIiRp5PAaZrqeglhZ4z73HAhMVJI5rSTTAUK3BmjBzHCR2ivCNvmKAvTRSv0bZDvGjfSB2GENwq1duU8S0jsS3Bgtxt-P5YEUi6M8',
    iconName: 'extension',
    description: 'Master 14 essential coding patterns (Sliding Window, Two Pointers, Fast & Slow Pointers) to solve hundreds of algorithmic problems quickly.',
    whatYouWillLearn: [
      'Nắm vững 14 mẫu tư duy giải thuật phổ biến',
      'Giải quyết nhanh các bài toán Sliding Window & Two Pointers',
      'Ứng dụng Heap, Stack và BFS/DFS giải quyết bài toán khó',
      'Kỹ thuật tối ưu hóa bộ nhớ và thời gian chạy tối đa'
    ],
    syllabus: []
  },
  {
    id: 'c20',
    title: 'System Design Fundamentals for Scale',
    category: 'Core Knowledge',
    author: 'Martin Kleppmann',
    authorTitle: 'Distributed Systems Researcher',
    authorAvatar: 'https://ui-avatars.com/api/?name=Martin+Kleppmann&background=F36F21&color=fff',
    authorBio: 'Martin Kleppmann is the author of \'Designing Data-Intensive Applications\' and specializes in data storage layouts and network structures.',
    rating: 4.9,
    enrolled: 3100,
    price: 1950000,
    originalPrice: 2990000,
    level: 'advanced',
    languages: ['java'],
    releaseDate: '2026-05-21',
    thumbnailUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDprSKVqEq347pPqZ9M8ZWp_6T-Pvi_68sA90ExU-mSJXsImRMFa4q4dLHkArN6WOv5WFywpvaSZBRAHvu_Dx0r6w9yK_mlTECqCeq9Wg3oBbgZTv9n5f5XBS7cYcelKHCSqcutDcmpUqgS0-UThBEEYGjKVVlqjNkMD5LeFuWllGb4uhmZZ8l2nvSElcuet9dv6J2P59fo1VSbODozVKEkm5a4gpdTPT1T6CEHtGUDY7Lv6jRnLSmwUI2aNOpki1r5UtOOo4ccDQQ',
    iconName: 'grid_view',
    description: 'Learn key architectural fundamentals behind database replication models, sharding layouts, message queues, and high-availability structures.',
    whatYouWillLearn: [
      'Thiết kế kiến trúc hệ thống chịu tải cao (High Scalability)',
      'Hiểu sâu cơ chế Database Replication & Partitioning',
      'Ứng dụng Caching (Redis/Memcached) giảm thiểu nghẽn cổ chai',
      'Thiết lập Message Queues (Kafka/RabbitMQ) cho luồng xử lý không đồng bộ'
    ],
    syllabus: []
  }
];

export const getCourseById = async (id: string): Promise<Course | undefined> => {
  return mockCourses.find(c => c.id === id);
};

export const getEnrolledCourses = async (enrolledIds: string[]): Promise<Course[]> => {
  return mockCourses.filter(c => enrolledIds.includes(c.id));
};

// --- BACKEND API INTEGRATION ---
const BASE_URL = 'http://localhost:8080/nonstopcoding';

export interface CourseListItemResponse {
  id: number;
  title: string;
  thumbnailUrl: string;
  shortDescription: string;
  price: number;
  averageRating: number;
  totalReviews: number;
  totalEnrolled: number;
  enrolled: boolean;
  progressPercentage: number;
  instructorName: string;
  categoryName?: string; // Mapped dynamically in frontend or backend
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

export interface ApiResponse<T> {
  status: number;
  code: number;
  message: string;
  result: T;
  timestamp: string;
}

export interface CourseSearchRequestParams {
  keyword?: string;
  categoryIds?: number[];
  minPrice?: number;
  maxPrice?: number;
  minRating?: number;
  maxRating?: number;
  instructorName?: string;
  page?: number;
  size?: number;
  sortBy?: string[];
  order?: string[];
}

export const fetchCourses = async (params: CourseSearchRequestParams): Promise<PageResponse<CourseListItemResponse>> => {
  const queryParams = new URLSearchParams();

  if (params.keyword && params.keyword.trim() !== '') {
    queryParams.append('keyword', params.keyword.trim());
  }

  if (params.categoryIds && params.categoryIds.length > 0) {
    params.categoryIds.forEach(id => queryParams.append('categoryIds', id.toString()));
  }

  if (params.minPrice !== undefined && params.minPrice !== null) {
    queryParams.append('minPrice', params.minPrice.toString());
  }

  if (params.maxPrice !== undefined && params.maxPrice !== null) {
    queryParams.append('maxPrice', params.maxPrice.toString());
  }

  if (params.minRating !== undefined && params.minRating !== null) {
    queryParams.append('minRating', params.minRating.toString());
  }

  if (params.maxRating !== undefined && params.maxRating !== null) {
    queryParams.append('maxRating', params.maxRating.toString());
  }

  if (params.instructorName && params.instructorName.trim() !== '') {
    queryParams.append('instructorName', params.instructorName.trim());
  }

  if (params.page !== undefined && params.page !== null) {
    queryParams.append('page', params.page.toString());
  }

  if (params.size !== undefined && params.size !== null) {
    queryParams.append('size', params.size.toString());
  }

  if (params.sortBy && params.sortBy.length > 0) {
    params.sortBy.forEach(field => queryParams.append('sortBy', field));
  }

  if (params.order && params.order.length > 0) {
    params.order.forEach(ord => queryParams.append('order', ord));
  }

  const response = await fetch(`${BASE_URL}/courses?${queryParams.toString()}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include', // Important: sends session cookie for personalized enrollment status
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Không thể tải danh sách khóa học');
  }

  const data: ApiResponse<PageResponse<CourseListItemResponse>> = await response.json();
  return data.result;
};

export interface CourseDetailResponse {
  id: number;
  title: string;
  thumbnailUrl: string;
  shortDescription: string;
  longDescription: string;
  whatYouLearn?: string;
  courseHighlight?: string;
  technologyTool?: string;
  prerequisites?: string;
  targetAudience?: string;
  completionBenefits?: string;
  price: number;
  averageRating: number;
  totalReviews: number;
  totalEnrolled: number;
  totalLessons: number;
  totalQuizzes: number;
  totalVideos: number;
  enrolled: boolean;
  progressPercentage: number;
  instructorName: string;
  instructorTitle?: string;
  instructorBio?: string;
  instructorAvatarUrl?: string;
  categoryName?: string;
  type?: string;
}

export const fetchCourseDetail = async (id: number | string): Promise<CourseDetailResponse> => {
  const response = await fetch(`${BASE_URL}/courses/${id}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Không thể tải chi tiết khóa học');
  }

  const data: ApiResponse<CourseDetailResponse> = await response.json();
  return data.result;
};

export interface CurriculumLessonResponse {
  id: number;
  title: string;
  isTrial: boolean;
  orderIndex: number;
  videoUrl?: string;
  type: 'video' | 'coding' | 'reading';
}

export interface CurriculumChapterResponse {
  id: number;
  title: string;
  orderIndex: number;
  lessons: CurriculumLessonResponse[];
}

export const fetchCourseCurriculum = async (id: number | string): Promise<CurriculumChapterResponse[]> => {
  const response = await fetch(`${BASE_URL}/courses/${id}/curriculum`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Không thể tải chương trình học');
  }

  const data: ApiResponse<CurriculumChapterResponse[]> = await response.json();
  return data.result;
};

export interface CourseReviewDto {
  id: number;
  content: string;
  star: number;
  displayName: string;
  avatarUrl?: string;
  createdAt: string;
}

export interface CourseReviewStatsResponse {
  averageRating: number;
  totalReviews: number;
  starDistribution: Record<number, number>;
  myReview?: CourseReviewDto;
  reviews: PageResponse<CourseReviewDto>;
}

export const fetchCourseReviews = async (id: number | string, page: number = 0, size: number = 10): Promise<CourseReviewStatsResponse> => {
  const queryParams = new URLSearchParams();
  queryParams.append('page', page.toString());
  queryParams.append('size', size.toString());

  const response = await fetch(`${BASE_URL}/courses/${id}/reviews?${queryParams.toString()}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Không thể tải đánh giá khóa học');
  }

  const data: ApiResponse<CourseReviewStatsResponse> = await response.json();
  return data.result;
};

export const submitCourseReview = async (id: number | string, star: number, content: string): Promise<void> => {
  const response = await fetch(`${BASE_URL}/courses/${id}/reviews`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
    body: JSON.stringify({ star, content })
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Không thể gửi đánh giá');
  }
};

export interface LearningDetailResponse {
  courseId: number;
  courseTitle: string;
  instructorName: string;
  progressPercentage: number;
  activeLessonId?: number;
  activeLessonTitle?: string;
  activeLessonVideoUrl?: string;
  activeLessonTheoryContent?: string;
}

export interface LearningCurriculumLessonResponse {
  id: number;
  title: string;
  isTrial: boolean;
  orderIndex: number;
  type: string;
  isCompleted: boolean;
  status?: string;
}

export interface LearningCurriculumChapterResponse {
  id: number;
  title: string;
  orderIndex: number;
  lessons: LearningCurriculumLessonResponse[];
}

export interface LearningLessonResponse {
  id: number;
  title: string;
  videoUrl: string;
  theoryContent: string;
  sourceCode?: string;
  problems?: any[];
  quiz?: any;
  exercises?: any[];
  status?: string;
}

export const fetchCourseLearningDetail = async (id: number | string): Promise<LearningDetailResponse> => {
  const response = await fetch(`${BASE_URL}/courses/${id}/learning-detail`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Không thể tải thông tin học tập');
  }

  const data: ApiResponse<LearningDetailResponse> = await response.json();
  return data.result;
};

export const fetchCourseLearningCurriculum = async (id: number | string): Promise<LearningCurriculumChapterResponse[]> => {
  const response = await fetch(`${BASE_URL}/courses/${id}/learning-curriculum`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Không thể tải chương trình học tập');
  }

  const data: ApiResponse<LearningCurriculumChapterResponse[]> = await response.json();
  return data.result;
};

export const fetchLearningLessonDetail = async (courseId: number | string, lessonId: number | string): Promise<LearningLessonResponse> => {
  const response = await fetch(`${BASE_URL}/courses/${courseId}/lessons/${lessonId}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Không thể tải chi tiết bài học');
  }

  const data: ApiResponse<LearningLessonResponse> = await response.json();
  return data.result;
};

export interface LessonComment {
  id: number;
  author: string;
  avatar_url?: string;
  text: string;
  createdAt: string;
  parentId?: number | null;
  replies: LessonComment[];
}

export interface CreateCommentRequest {
  content: string;
  parentId?: number | null;
}

export const fetchLessonComments = async (lessonId: number | string): Promise<LessonComment[]> => {
  const response = await fetch(`${BASE_URL}/courses/lessons/${lessonId}/comments`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Không thể tải bình luận bài học');
  }

  const data: ApiResponse<LessonComment[]> = await response.json();
  return data.result;
};

export const postLessonComment = async (lessonId: number | string, data: CreateCommentRequest): Promise<LessonComment> => {
  const response = await fetch(`${BASE_URL}/courses/lessons/${lessonId}/comments`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
    credentials: 'include',
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Không thể gửi bình luận');
  }

  const resData: ApiResponse<LessonComment> = await response.json();
  return resData.result;
};

export interface QuizOption {
  optionId: number;
  content: string;
  orderIndex: number;
  isCorrect?: boolean;
}

export interface QuizQuestion {
  questionId: number;
  content: string;
  orderIndex: number;
  options: QuizOption[];
  selectedOptionId?: number | null;
  correctOptionId?: number;
  isCorrect?: boolean;
}

export interface QuizDetail {
  quizId: number;
  title: string;
  submitted: boolean;
  score?: number;
  totalQuestion?: number;
  correctQuestion?: number;
  submittedAt?: string;
  questions: QuizQuestion[];
}

export interface QuizAnswerItem {
  questionId: number;
  selectedOptionId: number | null;
}

export interface QuizSubmitRequest {
  answers: QuizAnswerItem[];
}

export interface QuizOptionResult {
  optionId: number;
  content: string;
  orderIndex: number;
  isCorrect: boolean;
}

export interface QuizQuestionResult {
  questionId: number;
  content: string;
  selectedOptionId: number | null;
  correctOptionId: number;
  isCorrect: boolean;
  options: QuizOptionResult[];
}

export interface QuizSubmitResult {
  attemptId: number;
  totalQuestion: number;
  correctQuestion: number;
  score: number;
  submittedAt: string;
  results: QuizQuestionResult[];
}

export const fetchQuizByLesson = async (courseId: number | string, lessonId: number | string): Promise<QuizDetail> => {
  const response = await fetch(`${BASE_URL}/courses/${courseId}/lessons/${lessonId}/quiz`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Không thể tải chi tiết bài trắc nghiệm');
  }

  const data: ApiResponse<QuizDetail> = await response.json();
  return data.result;
};

export const submitQuiz = async (courseId: number | string, quizId: number | string, data: QuizSubmitRequest): Promise<QuizSubmitResult> => {
  const response = await fetch(`${BASE_URL}/courses/${courseId}/quizzes/${quizId}/submit`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
    credentials: 'include',
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Không thể nộp bài trắc nghiệm');
  }

  const resData: ApiResponse<QuizSubmitResult> = await response.json();
  return resData.result;
};

export const completeLesson = async (courseId: number | string, lessonId: number | string): Promise<void> => {
  const response = await fetch(`${BASE_URL}/courses/${courseId}/lessons/${lessonId}/complete`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Không thể hoàn thành bài học');
  }
};


