// Mock data removed - use API calls from dashboardService and courseService instead
export const initialMyCourses = [];


export interface ProblemDetail {
  difficulty: string;
  difficultyClass: string;
  description: string;
  code: Record<string, string>;
}

export const problemData: Record<string, ProblemDetail> = {
  "Two Sum": {
    difficulty: "Easy",
    difficultyClass: "bg-green-50 text-brand-green border border-green-150",
    description: `
      <p class="mb-4">Given an array of integers <code class="bg-surface-gray px-1.5 py-0.5 rounded border border-gray-200 font-mono text-xs">nums</code> and an integer <code class="bg-surface-gray px-1.5 py-0.5 rounded border border-gray-200 font-mono text-xs">target</code>, return <em>indices of the two numbers such that they add up to <code class="bg-surface-gray px-1.5 py-0.5 rounded border border-gray-200 font-mono text-xs">target</code></em>.</p>
      <p class="mb-4">You may assume that each input would have <strong>exactly one solution</strong>, and you may not use the same element twice.</p>
      <p class="mb-4">You can return the answer in any order.</p>
      <div class="grid md:grid-cols-2 gap-4 my-4">
          <div class="bg-surface-gray border border-gray-200 p-4 rounded-xl">
              <p class="font-bold text-xs text-text-main mb-2">Example 1:</p>
              <div class="font-mono text-xs text-text-muted space-y-1">
                  <p><span class="text-text-main font-semibold">Input:</span> nums = [2,7,11,15], target = 9</p>
                  <p><span class="text-text-main font-semibold">Output:</span> [0,1]</p>
                  <p><span class="text-text-main font-semibold">Explanation:</span> Because nums[0] + nums[1] == 9, we return [0, 1].</p>
              </div>
          </div>
          <div class="bg-surface-gray border border-gray-200 p-4 rounded-xl">
              <p class="font-bold text-xs text-text-main mb-2">Example 2:</p>
              <div class="font-mono text-xs text-text-muted space-y-1">
                  <p><span class="text-text-main font-semibold">Input:</span> nums = [3,2,4], target = 6</p>
                  <p><span class="text-text-main font-semibold">Output:</span> [1,2]</p>
              </div>
          </div>
      </div>
      <div class="space-y-2 mb-4">
          <h4 class="font-bold text-xs text-text-main">Constraints:</h4>
          <ul class="list-disc pl-5 text-xs text-text-muted space-y-1">
              <li><code class="bg-surface-gray px-1 rounded font-mono text-[11px]">2 &lt;= nums.length &lt;= 10<sup>4</sup></code></li>
              <li><code class="bg-surface-gray px-1 rounded font-mono text-[11px]">-10<sup>9</sup> &lt;= nums[i] &lt;= 10<sup>9</sup></code></li>
              <li><code class="bg-surface-gray px-1 rounded font-mono text-[11px]">-10<sup>9</sup> &lt;= target &lt;= 10<sup>9</sup></code></li>
              <li>Only one valid answer exists.</li>
          </ul>
      </div>
    `,
    code: {
      "Java": `class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Write your Java code here\n        return new int[] {};\n    }\n}`,
      "C++": `class Solution {\npublic:\n    vector<int> twoSum(vector<int>& nums, int target) {\n        // Write your C++ code here\n        return {};\n    }\n};`,
      "Python": `class Solution:\n    def twoSum(self, nums: List[int], target: int) -> List[int]:\n        # Write your Python code here\n        pass`
    }
  },
  "Reverse Linked List": {
    difficulty: "Easy",
    difficultyClass: "bg-green-50 text-brand-green border border-green-150",
    description: `
      <p class="mb-4">Given the <code class="bg-surface-gray px-1.5 py-0.5 rounded border border-gray-200 font-mono text-xs">head</code> of a singly linked list, reverse the list, and return <em>its reversed list</em>.</p>
      <div class="grid md:grid-cols-2 gap-4 my-4">
          <div class="bg-surface-gray border border-gray-200 p-4 rounded-xl">
              <p class="font-bold text-xs text-text-main mb-2">Example 1:</p>
              <div class="font-mono text-xs text-text-muted space-y-1">
                  <p><span class="text-text-main font-semibold">Input:</span> head = [1,2,3,4,5]</p>
                  <p><span class="text-text-main font-semibold">Output:</span> [5,4,3,2,1]</p>
              </div>
          </div>
          <div class="bg-surface-gray border border-gray-200 p-4 rounded-xl">
              <p class="font-bold text-xs text-text-main mb-2">Example 2:</p>
              <div class="font-mono text-xs text-text-muted space-y-1">
                  <p><span class="text-text-main font-semibold">Input:</span> head = [1,2]</p>
                  <p><span class="text-text-main font-semibold">Output:</span> [2,1]</p>
              </div>
          </div>
      </div>
      <div class="space-y-2 mb-4">
          <h4 class="font-bold text-xs text-text-main">Constraints:</h4>
          <ul class="list-disc pl-5 text-xs text-text-muted space-y-1">
              <li>The number of nodes in the list is the range <code class="bg-surface-gray px-1 rounded font-mono text-[11px]">[0, 5000]</code>.</li>
              <li><code class="bg-surface-gray px-1 rounded font-mono text-[11px]">-5000 &lt;= Node.val &lt;= 5000</code></li>
          </ul>
      </div>
    `,
    code: {
      "Java": `/**\n * Definition for singly-linked list.\n * public class ListNode {\n *     int val;\n *     ListNode next;\n *     ListNode() {}\n *     ListNode(int val) { this.val = val; }\n *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }\n * }\n */\nclass Solution {\n    public ListNode reverseList(ListNode head) {\n        // Write your Java code here\n        return null;\n    }\n}`,
      "C++": `/**\n * Definition for singly-linked list.\n * struct ListNode {\n *     int val;\n *     ListNode *next;\n *     ListNode() : val(0), next(nullptr) {}\n *     ListNode(x) : val(x), next(nullptr) {}\n *     ListNode(x, ListNode *next) : val(x), next(next) {}\n * };\n */\nclass Solution {\npublic:\n    ListNode* reverseList(ListNode* head) {\n        // Write your C++ code here\n        return nullptr;\n    }\n};`,
      "Python": `# Definition for singly-linked list.\n# class ListNode:\n#     def __init__(self, val=0, next=None):\n#         self.val = val\n#         self.next = next\nclass Solution:\n    def reverseList(self, head: Optional[ListNode]) -> Optional[ListNode]:\n        # Write your Python code here\n        pass`
    }
  },
  "Spring Context Hierarchy Solver": {
    difficulty: "Medium",
    difficultyClass: "bg-primary-light/50 text-primary border border-primary/20",
    description: `
      <p class="mb-4">Given a hierarchical relationship of Spring ApplicationContext names and their respective registered beans, resolve if a child context can correctly lookup a bean defined in its parent context or its own context, following standard hierarchical bean lookup rules.</p>
      <div class="grid md:grid-cols-2 gap-4 my-4">
          <div class="bg-surface-gray border border-gray-200 p-4 rounded-xl">
              <p class="font-bold text-xs text-text-main mb-2">Example 1:</p>
              <div class="font-mono text-xs text-text-muted space-y-1">
                  <p><span class="text-text-main font-semibold">Input:</span> contextParents = { "child": "parent" }, contextBeans = { "parent": ["userService"], "child": ["orderService"] }, lookupContext = "child", beanName = "userService"</p>
                  <p><span class="text-text-main font-semibold">Output:</span> true</p>
                  <p><span class="text-text-main font-semibold">Explanation:</span> The child context can find the "userService" bean because it is defined in its parent context.</p>
              </div>
          </div>
          <div class="bg-surface-gray border border-gray-200 p-4 rounded-xl">
              <p class="font-bold text-xs text-text-main mb-2">Example 2:</p>
              <div class="font-mono text-xs text-text-muted space-y-1">
                  <p><span class="text-text-main font-semibold">Input:</span> contextParents = { "child": "parent" }, contextBeans = { "parent": ["userService"], "child": ["orderService"] }, lookupContext = "parent", beanName = "orderService"</p>
                  <p><span class="text-text-main font-semibold">Output:</span> false</p>
                  <p><span class="text-text-main font-semibold">Explanation:</span> The parent context cannot see beans defined in the child context.</p>
              </div>
          </div>
      </div>
      <div class="space-y-2 mb-4">
          <h4 class="font-bold text-xs text-text-main">Constraints:</h4>
          <ul class="list-disc pl-5 text-xs text-text-muted space-y-1">
              <li>Lookups must trace parents recursively until the root context is reached.</li>
              <li>Context names and Bean names are case-sensitive.</li>
          </ul>
      </div>
    `,
    code: {
      "Java": `class Solution {\n    public boolean resolveBeanLookup(Map<String, String> contextParents, Map<String, List<String>> contextBeans, String lookupContext, String beanName) {\n        // Write your Java code here\n        return false;\n    }\n}`,
      "C++": `class Solution {\npublic:\n    bool resolveBeanLookup(unordered_map<string, string>& contextParents, unordered_map<string, vector<string>>& contextBeans, string lookupContext, string beanName) {\n        // Write your C++ code here\n        return false;\n    }\n};`,
      "Python": `class Solution:\n    def resolveBeanLookup(self, contextParents: Dict[str, str], contextBeans: Dict[str, List[str]], lookupContext: str, beanName: str) -> bool:\n        # Write your Python code here\n        return False`
    }
  }
};

