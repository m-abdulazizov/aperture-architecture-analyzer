# Vulnerable Spring Sample

This tiny project is intentionally flawed so Aperture can produce a useful demo report.

To scan it manually:

```powershell
Compress-Archive -Path .\samples\vulnerable-spring\src -DestinationPath .\build\tmp\vulnerable-spring.zip
```

Then create a project in Aperture, upload `vulnerable-spring.zip`, and run a scan.
