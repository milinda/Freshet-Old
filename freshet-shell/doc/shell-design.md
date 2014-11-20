# Freshet Shell Design

## Workflow

### Single Node YARN

- User clone/download Freshet code
- Run 'setup.sh local' inside freshet-shell/bin. 'setup.sh local' takes care of running 'grid bootstrap' from freshet-shell/bin.
- Run fshell inside freshet-shell/bin and start writing queries


### Multiple Node YARN

- User clone/download Freshet code
- Copy yarn-site.xml, core-site.xml and hdfs-site.xml to freshet-shell/conf
- Run 'setup.sh remote' insie freshet-shell/bin
- Run fshell inside freshet-shell/bin and start writing queries
