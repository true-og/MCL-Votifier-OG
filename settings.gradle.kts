rootProject.name = "MCL-Votifier-OG"

// Execute bootstrap.sh
exec {
    workingDir(rootDir)
    commandLine("sh", "bootstrap.sh")
}

include("libs:Utilities-OG")
