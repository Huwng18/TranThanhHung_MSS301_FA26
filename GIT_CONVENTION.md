# Git Commit Conventions - HSF302 Project

Quy ước commit message theo chuẩn Conventional Commits 1.0.0 — chuẩn industry-wide, được Angular, Vue, NestJS, GitHub CLI, và nhiều project lớn dùng.
Tham khảo: [https://www.conventionalcommits.org/](https://www.conventionalcommits.org/)

## 1. Format tổng quát
```
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

**Cấu trúc**
- `type` (Bắt buộc): Loại commit (feat, fix, docs…). Xem danh sách Type
- `scope` (Tuỳ chọn): Phần code bị ảnh hưởng (entity, service, ci…)
- `subject` (Bắt buộc): Tóm tắt thay đổi (≤ 50 ký tự, imperative mood)
- `body` (Tuỳ chọn): Giải thích what và why, không phải how. Wrap 72 char
- `footer` (Tuỳ chọn): Reference issue, breaking change

**Ví dụ đầy đủ**
```
feat(repository): add findByEmail method to StudentRepository

Bổ sung method `Optional<Student> findByEmail(String email)` để
support feature login bằng email. Method dùng derived query của
Spring Data JPA, không cần viết JPQL.

Closes #42
```

## 2. Danh sách Type
- `feat`: Thêm tính năng mới cho user (VD: `feat: add CRUD operations for Book entity`)
- `fix`: Sửa bug (VD: `fix: prevent NullPointerException in StudentDAO.update()`)
- `docs`: Chỉ sửa documentation (VD: `docs: update README with H2 setup instructions`)
- `style`: Format code (whitespace, semicolon, indent…) — không đổi logic (VD: `style: reformat StudentServiceImpl with google-java-format`)
- `refactor`: Đổi cấu trúc code, KHÔNG thêm feature, KHÔNG sửa bug (VD: `refactor: extract validateStudent into separate validator class`)
- `perf`: Cải thiện performance (VD: `perf: add @EntityGraph to avoid N+1 query in findAll()`)
- `test`: Thêm/sửa test (VD: `test: add unit tests for StudentServiceImpl validation`)
- `chore`: Thay đổi không ảnh hưởng src/test (build, deps…) (VD: `chore: bump Spring Boot from 3.5.0 to 3.5.14`)
- `build`: Sửa build system, Maven, dependency (VD: `build: add h2database test dependency to pom.xml`)
- `ci`: Sửa CI config (GitHub Actions…) (VD: `ci: add Java 21 matrix to classroom.yml`)
- `revert`: Revert commit trước (VD: `revert: feat(service): add caching to getById()`)

*Nguyên tắc: Khi không chắc, dùng chore. Nhưng cố gắng dùng đúng type.*

## 3. Quy tắc viết Subject
**DO — Nên làm**
- Imperative mood ("add", "fix", "update" — như command) (VD: `add login validation`)
- Viết thường (lower case) (VD: `fix typo in error message`)
- Không có dấu chấm cuối (VD: `update README`)
- ≤ 50 ký tự (VD: `fix StudentDAO update method`)
- Trả lời câu: "If applied, this commit will _____"

**DON'T — Không nên**
- Added new feature (past tense) -> Sai. Nên: `add new feature`
- Fixes bug in service (3rd person) -> Sai. Nên: `fix bug in service`
- update. (có chấm) -> Sai. Nên: `update`
- Update README.md with very long description… (>50 ký tự) -> Sai. Nên: `docs: update README with H2 setup`
- stuff / wip / fix (vô nghĩa) -> Sai. Nên: `fix: handle null email in StudentService.create()`
- Update files (mơ hồ) -> Sai. Nên: `refactor: extract DTO mapper from controller`

## 4. Cheat Sheet 1 trang
```text
┌─────────────────────────────────────────────────────────────────┐
│              CONVENTIONAL COMMITS CHEAT SHEET                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│ FORMAT: <type>(<scope>): <subject>                              │
│                                                                 │
│ TYPES:                                                          │
│   feat     → New feature                                        │
│   fix      → Bug fix                                            │
│   docs     → Documentation only                                 │
│   style    → Formatting, no logic change                        │
│   refactor → Restructure code, no behavior change               │
│   perf     → Performance improvement                            │
│   test     → Add/modify tests                                   │
│   chore    → Maintenance, deps update                           │
│   build    → Build system, Maven, npm                           │
│   ci       → CI/CD config (GitHub Actions...)                   │
│   revert   → Revert previous commit                             │
│                                                                 │
│ RULES:                                                          │
│   ✓ Imperative mood: "add" not "added" / "adds"                 │
│   ✓ Lower case subject                                          │
│   ✓ No period at end                                            │
│   ✓ Max 50 chars in subject                                     │
│   ✓ Body wraps at 72 chars                                      │
│                                                                 │
│ BREAKING CHANGE:                                                │
│   feat(api)!: change findById return Optional                   │
│   Or in footer:                                                 │
│   BREAKING CHANGE: <description>                                │
│                                                                 │
│ EXAMPLES:                                                       │
│   feat(dao): add save method with transaction                   │
│   fix(service): use exact error message for blank name          │
│   docs(readme): add H2 setup instructions                       │
│   test(entity): add JPA annotation tests                        │
│   refactor: extract validateStudent helper                      │
│   chore: bump Spring Boot to 3.5.14                             │
│   ci(github): add autograding workflow                          │
│                                                                 │
│ BRANCH: <type>/<short-desc>                                     │
│   feat/student-search-by-email                                  │
│   fix/transaction-leak-in-dao                                   │
│   docs/update-readme-h2-setup                                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```
