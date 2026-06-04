---
name: creating-methods-or-functions
description: When creating new methods or functions follow the guidelines described here
---

When creating a method or function, take into consideration these guidelines:

1. **Keep it short**: Limit the method body to ~10 lines (with a maximum of +5 lines spare).
2. **Limit the number of parameters**: Allow a maximum of three parameters. If more are needed, group the related extras into a data class.
3. **Group parameters semantically**: When creating a data class to hold parameters, group only fields that are genuinely related to each other.
4. **Make the name explain the purpose**: Since each method is small, its name must make the intent obvious — no comment should be needed to understand what it does.
5. **Add a doc comment only for genuinely complex signatures**: Only document when the parameter meaning or the interaction between calls would not be obvious to a reader unfamiliar with the context.

These rules do not apply to composable functions — see `creating-composables` for those guidelines.