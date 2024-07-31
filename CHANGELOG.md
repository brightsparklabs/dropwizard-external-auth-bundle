# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

The changelog is applicable from version `2.0.0` onwards.

---

## [Unreleased] - YYYY-MM-DD

[Unreleased]: https://github.com/brightsparklabs/appcli/compare/2.0.0...HEAD

### Added

### Changed

- TERA-1723: Replaced usages of the DropWizard `AuthenticationException` with 
  `AuthenticationDeniedException` as the former should only be used for internal errors and not
  for issues with invalid or missing credentials.

### Deprecated

### Removed

### Fixed

### Security

---

## [2.0.0] - 2024-06-21

[2.0.0]: https://github.com/brightsparklabs/appcli/compare/1.0.0...2.0.0

### Added

- TERA-1397: Add exception mappers for the Authentication exceptions.
- TERA-1438: Support Dropwizard 3.0.0

### Changed

- TERA-1397: Looks for `roles` in top level key, and be tolerant if not found. Also support groups.

---

## [1.0.0] - 2023-05-05

[1.0.0]: https://github.com/brightsparklabs/appcli/compare/0.0.0...1.0.0

_No changelog for this release._

---

# Template

## [Unreleased] - YYYY-MM-DD

[Unreleased]: https://github.com/brightsparklabs/appcli/compare/x.y.z...HEAD

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

---
