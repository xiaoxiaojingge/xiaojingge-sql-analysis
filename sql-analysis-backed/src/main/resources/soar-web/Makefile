.DEFAULT_GOAL:=help

COLOR_ENABLE=$(shell tput colors > /dev/null; echo $$?)
ifeq "$(COLOR_ENABLE)" "0"
CRED:=$(shell tput setaf 1 2>/dev/null)
CGREEN:=$(shell tput setaf 2 2>/dev/null)
CYELLOW:=$(shell tput setaf 3 2>/dev/null)
CBLUE:=$(shell tput setaf 4 2>/dev/null)
CMAGENTA:=$(shell tput setaf 5 2>/dev/null)
CCYAN:=$(shell tput setaf 6 2>/dev/null)
CWHITE:=$(shell tput setaf 7 2>/dev/null)
CEND:=$(shell tput sgr0 2>/dev/null)
endif

.PHONY: fmt
fmt:   ## fmt code
	@echo "$(CGREEN)Run gofmt on all source files ...$(CEND)"
	@echo "gofmt -l -s -w ..."
	@ret=0 && for d in $$(go list -f '{{.Dir}}' ./... | grep -v /vendor/); do \
		gofmt -l -s -w $$d/*.go || ret=$$? ; \
	done ; exit $$ret

.PHONY: deps
deps:			## deps check
	@echo "$(CGREEN)deps check ...$(CEND)"
	@/bin/bash script/dev/deps.sh


.PHONY: lint
lint:   deps   	## check lint
	@echo "$(CGREEN)golangci-lint run ./... ...$(CEND)"
	golangci-lint run ./...

.PHONY: build
build:     	## build current target
	@echo "$(CGREEN)soar-web build snapshot no publish ...$(CEND)"
	@goreleaser build --snapshot --rm-dist  --single-target -f scripts/build/build.yml

.PHONY: buildall
buildall:      	## build all
	@echo "$(CGREEN)soar-web build snapshot no publish ...$(CEND)"
	@goreleaser build --snapshot --rm-dist -f scripts/build/build.yml
.PHONY: web

snapshot:   fmt 	## pre snapshot
	@echo "$(CGREEN)soar-web release snapshot no publish ...$(CEND)"
	@goreleaser release --skip-publish  --snapshot --rm-dist -f scripts/build/build.yml
.PHONY: release
release:   fmt	## release no publish
	@echo "$(CGREEN)soar-web release no publish ...$(CEND)"
	@goreleaser release --skip-publish  --rm-dist -f scripts/build/build.yml

.PHONY: clean
clean:      	## clean up
	@echo "$(CGREEN)clean up dist ...$(CEND)"
	@rm -rf ./dist


.PHONY: help
help:			## Show this help.
	@echo "$(CGREEN)soar-web project$(CEND)"
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make $(CYELLOW)<target>$(CEND) (default: help)\n\nTargets:\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  $(CCYAN)%-12s$(CEND) %s\n", $$1, $$2 }' $(MAKEFILE_LIST)
