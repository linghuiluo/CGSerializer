## Build
1. Build and install soot
```
git clone https://github.com/soot-oss/soot.git
cd soot
git checkout fe86c3e73d9b7bcd810dadd2aa81351d4288642d
mvn install -DskipTests
```
2. Build and install CGSerializer
```
git clone https://github.com/linghuiluo/CGSerializer
cd CGSerializer
mvn install
```