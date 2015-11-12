angular.module('xodus').service('EntitiesService', ['$http', '$q', '$location', function($http, $q, $location) {
    var propertyTypes = [
        {
            displayName: 'String',
            clazz: 'java.lang.String',
            maxValue: null,
            minValue: null,
            readonly: false
        }, {
            displayName: 'Short',
            clazz: 'java.lang.Short',
            maxValue: -32768,
            minValue: 32767,
            readonly: false
        }, {
            displayName: 'Integer',
            clazz: 'java.lang.Integer',
            maxValue: -2147483648,
            minValue: 2147483647,
            readonly: false
        }, {
            displayName: 'Long',
            clazz: 'java.lang.Long',
            maxValue: null,
            minValue: null,
            readonly: false
        }, {
            displayName: 'Double',
            clazz: 'java.lang.Double',
            maxValue: null,
            minValue: null,
            readonly: false
        }, {
            displayName: 'Float',
            clazz: 'java.lang.Float',
            maxValue: null,
            minValue: null,
            readonly: false
        }, {
            displayName: 'Byte',
            clazz: 'java.lang.Byte',
            maxValue: -128,
            minValue: 127,
            readonly: false
        }
    ];

    this.allTypes = allTypes;
    this.newProperty = newProperty;
    this.fromProperty = fromProperty;
    this.newEntity = newEntity;
    this.getChangeSummary = getChangeSummary;
    this.save = save;
    this.byId = byId;

    function allTypes() {
        return angular.copy(propertyTypes);
    }

    function newProperty() {
        return {
            name: null,
            value: null,
            type: angular.copy(propertyTypes[0])
        };
    }

    function fromProperty(property) {
        return angular.copy(property);
    }

    function newEntity(typeId) {
        return {
            type: propertyTypes[0],
            typeId: typeId,
            properties: [],
            blobs: [],
            links: []
        };
    }

    function getChangeSummary(initial, modified) {
        var changeSummary = {
            properties: {
                added: [],
                deleted: [],
                modified: []
            },
            links: {
                added: [],
                deleted: [],
                modified: []
            }
        };
        processChangeSummary(changeSummary, initial, modified, sectionOf('properties'),
            function(initialProperty, modifiedProperty) {
                if (initialProperty.type.clazz === modifiedProperty.type.clazz) {
                    if (initialProperty.value !== modifiedProperty.value) {
                        changeSummary.properties.modified.push(modifiedProperty);
                    }
                } else {
                    changeSummary.properties.deleted.push(initialProperty);
                    changeSummary.properties.added.push(modifiedProperty);
                }
            });
        processChangeSummary(changeSummary, initial, modified, sectionOf('links'),
            function(initialLink, modifiedLink) {
                changeSummary.links.modified.push(modifiedLink);
            });
        return changeSummary;
    }

    function save(entity, changeSummary) {
        var isNew = !angular.isDefined(entity.id);
        var path = 'api/type/' + entity.typeId + '/entity';
        if (!isNew) {
            path = path + '/' + entity.id
        }
        if (isNew) {
            return $http.post(path, changeSummary);
        }
        return $http.put(path, changeSummary);

    }

    function findByName(array, name) {
        var result = array.filter(function(named) {
            return named.name === name;
        });
        return result.length ? result[0] : null;
    }

    function join(namedArray1, namedArray2) {
        var joined = namedArray1.concat(namedArray2);
        var uniqueNames = {};
        angular.forEach(joined, function(item) {
            if (!uniqueNames[item.name]) {
                uniqueNames[item.name] = null;
            }
        });
        return {
            joined: joined,
            uniqueNames: Object.keys(uniqueNames)
        };
    }

    function processChangeSummary(changeSummary, initial, modified, section, callback) {
        var initialSection = section(initial);
        var modifiedSection = section(modified);
        var summarySection = section(changeSummary);
        var joined = join(initialSection, modifiedSection);
        angular.forEach(joined.uniqueNames, function(name) {
            var initialProperty = findByName(initialSection, name);
            var modifiedProperty = findByName(modifiedSection, name);
            if (initialProperty && modifiedProperty) {
                callback(initialProperty, modifiedProperty);
            } else if (initialProperty) {
                summarySection.deleted.push(initialProperty);
            } else if (modifiedProperty) {
                summarySection.added.push(modifiedProperty);
            }
        });
    }

    function sectionOf(name) {
        return function(item) {
            return item[name];
        };
    }

    function byId(typeId, entityId) {
        if (!entityId) {
            return $q.when(newEntity(typeId));
        }
        return $http.get('api/type/' + typeId + '/entity/' + entityId).then(function(response) {
            return response.data;
        }, function() {
            $location.path('/error');
        });
    }

}]);
