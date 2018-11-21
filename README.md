# SourceWindowsInstaller
Code for a Windows installer of TorXakis

## Dependencies
We use [Wix](http://wixtoolset.org) for building the Windows installer.
Tested with Wix version 3.11.1.

## Usage
.\TxsCreateInstaller.bat 0.3.0 .\wxs.config

## How to publish a release


1) Tag the commit to be released in [TorXakis repository](https://github.com/TorXakis/TorXakis) with release version. The format should be: **vX.X.X** e.g. `v0.5.0`.

   `git tag -a v0.8.0 -m "release 2018 Q3"`
2) Push the tag to remote repository with: `git push --tags`
3) Tag the latest commit in [SourceWindowsInstaller repository][2] **with the same release version (e.g. `v0.5.0`)**.
4) Push the tag to remote repository with: `git push --tags`

   ==> As soon as you push the tag to [SourceWindowsInstaller repository][2] [AppVeyor][3] will pick it up, clone that version from TorXakis repository, build the installer, create a **draft release** on [GitHub releases of TorXakis][1] and push the installer to that release.

5) When the [AppVeyor build][3] completes, **draft release** will be ready on [GitHub Releases page of TorXakis][1]. Click `Edit` button at top right and update description of the release with highlights.
6) Clear `This is a pre-release` checkbox at the bottom and click `Update release` button to publish.

[1]: https://github.com/TorXakis/TorXakis/releases
[2]: https://github.com/TorXakis/SourceWindowsInstaller
[3]: https://ci.appveyor.com/project/torxakis-admin/sourcewindowsinstaller/history
