angular.module('xodus').controller('FormViewController', ['$scope', 'EntitiesService', 'EntityTypeService',
    function($scope, entities, types) {
        var formView = this;
        var state = null;
        formView.allLinksTypes = [];
        formView.entities = [];
        types.all().then(function(data) {
            formView.allLinksTypes = data;
            formView.newLink = newLink();
        });
        entities.byId($scope.entityTypeId, $scope.entityId).then(function(entity) {
            state = {
                initial: angular.copy(entity),
                current: angular.copy(entity),
                revert: function() {
                    this.current = angular.copy(entity);
                },
                update: function(newOne) {
                    this.initial = this.current;
                    this.current = newOne;
                }
            };
            initialize();
        });


        formView.newProperty = function() {
            $scope.properties.push(entities.newProperty());
        };
        formView.toggleView = function() {
            $scope.editMode = !$scope.editMode;
        };

        formView.removeProperty = function(property) {
            var index = $scope.properties.indexOf(property);
            $scope.properties.splice(index, 1);
        };

        formView.save = function() {
            var propsForm = $scope['propsForm'];
            var linksForm = $scope['linksForm'];
            makeDirty(propsForm);
            if (propsForm.$invalid) {
                return;
            }
            formView.editMode = false;
            state.current.links = $.map($scope.links, toBackendLink);
            var changeSummary = entities.getChangeSummary(state.initial, state.current);
            entities.save(state.initial, changeSummary).then(function(response) {
                state.update(response.data);
                initialize();
            }, function(response) {
                formView.error = response.data.msg;
                formView.editMode = true;
            });
        };

        formView.closeError = function() {
            formView.error = null;
        };

        formView.revert = function() {
            state.revert();
            initialize();
        };

        formView.cancel = function() {
            $scope.backToSearch();
        };
        formView.removeValue = function(property) {
            property.value = null;
        };
        formView.getMessage = getMessage;

        formView.hasError = function(formName, inputName) {
            var field = $scope[formName][inputName];
            return field && field.$invalid && field.$dirty;
        };
        formView.searchEntities = function(searchTerm) {
            types.search(formView.newLink.type.id, searchTerm, 0, 10).then(function(data) {
                formView.entities = data.items;
            });
        };

        function initialize() {
            formView.error = null;
            formView.isNew = !angular.isDefined(state.initial.id);
            $scope.properties = state.current.properties;
            $scope.blobs = state.current.blobs;
            $scope.links = $.map(state.current.links, toUILink);

            $scope.editMode = formView.isNew;
            formView.label = (formView.isNew ? 'New ' + state.initial.type : state.initial.label);
            formView.allTypes = entities.allTypes();
        }

        function makeDirty(form) {
            angular.forEach(Object.keys(form), function(key) {
                var value = form[key];
                if (value && angular.isFunction(value.$setDirty)) {
                    value.$setDirty(true);
                }
            });
        }

        function getMessage(form, name) {
            var field = $scope[form][name];
            if (('undefined' === typeof(field)) || field.$valid) {
                return undefined;
            }

            var message = '';
            if (field.$error['number']) {
                message += ' - not a number';
            }
            if (field.$error['min']) {
                message += ' - too small';
            }
            if (field.$error['max']) {
                message += ' - too large';
            }
            if (field.$error['pattern']) {
                message += ' - not match pattern';
            }
            if (field.$error['required'] && !message) {
                message += ' - required field';
            }
            return '\u2718' + message;
        }

        formView.removeLink = function() {
            return {
                type: formView.allLinksTypes[0],
                value: null
            }
        };
        formView.addNewLink = function() {
            var linksForm = $scope['linksForm'];
            makeDirty(linksForm);
            if (linksForm.$valid) {
                $scope.links.push(formView.newLink);
                formView.newLink = newLink();
                linksForm.$setPristine(true);
            }
        };

        function newLink() {
            return {
                name: null,
                type: formView.allLinksTypes[0],
                value: null
            }
        }

        function toBackendLink(link) {
            return {
                name: link.name,
                typeId: link.type.id,
                type: link.type.name,
                entityId: link.value.id
            }
        }

        function toUILink(link) {
            return {
                name: link.name,
                type: findType(link.typeId),
                id: link.typeId,
                label: link.label
            }
        }

        function findType(id) {
            var founded = null;
            angular.forEach(formView.allLinksTypes, function(type) {
                if (type.id == id) {
                    founded = type;
                }
            });
            return founded;
        }
    }]);