# Release process

Release process for this project involves:

1. Branch feature branch from `develop`.
2. Checkout feature branch. Implement feature. Test it.
3. Fire pull request to `develop`.
4. Test. Merge pull request. Delete feature branch.

Repeat 1-4 for all features.

5. Branch release branch from `develop`. Call it `release-<current-version>`.
6. Checkout release branch.
7. Update version in `build.gradle` to `<current-version>-RC1`.
8. Fire pull request to `master`.
9. Test. Merge pull request. Delete release branch.
10. Checkout `master`.
11. Update version to `<current-version>`.
12. Build.
13. Make release in GitHub. Don't forget to add release notes.
14. Deploy to production environment.
15. Merge `master` back to `develop`.
16. Checkout `develop`.
17. Update version to `<next-version>-SNAPSHOT`.

Continue your development.
