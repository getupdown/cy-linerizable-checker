#!/bin/zsh

numb=$(ps -ef | grep DistributedLockEntrance | grep -v "grep" | awk '{print $2}')

jstack -l "$numb"
