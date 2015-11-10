angular.module('xodus').service('EntitiesService', ['$http', function($http) {
    var propertyTypes = [
        {
            displayName: 'String',
            clazz: 'java.lang.String',
            maxValue: null,
            minValue: null
        }, {
            displayName: 'Short',
            clazz: 'java.lang.Short',
            maxValue: '',
            minValue: ''
        }, {
            displayName: 'Integer',
            clazz: 'java.lang.Integer',
            maxValue: '',
            minValue: ''
        }, {
            displayName: 'Long',
            clazz: 'java.lang.Long',
            maxValue: '',
            minValue: ''
        }, {
            displayName: 'Double',
            clazz: 'java.lang.Double',
            maxValue: '',
            minValue: ''
        }, {
            displayName: 'Float',
            clazz: 'java.lang.Float',
            maxValue: '',
            minValue: ''
        }, {
            displayName: 'Byte',
            clazz: 'java.lang.Byte',
            maxValue: '',
            minValue: ''
        }
    ];

    this.allTypes = allTypes;
    this.newProperty = newProperty;
    this.fromProperty = fromProperty;
    this.newEntity = newEntity;
    this.getChangeSummary = getChangeSummary;
    this.save = save;

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
        var joined = join(initial.properties, modified.properties);
        angular.forEach(joined.uniqueNames, function(name) {
            var initialProperty = findByName(initial.properties, name);
            var modifiedProperty = findByName(modified.properties, name);
            if (initialProperty && modifiedProperty) {
                if (initialProperty.type.clazz === modifiedProperty.type.clazz) {
                    if (initialProperty.value !== modifiedProperty.value) {
                        changeSummary.properties.modified.push(modifiedProperty);
                    }
                } else {
                    changeSummary.properties.deleted.push(initialProperty);
                    changeSummary.properties.added.push(modifiedProperty);
                }
            } else if (initialProperty) {
                changeSummary.properties.deleted.push(initialProperty);
            } else if (modifiedProperty) {
                changeSummary.properties.added.push(modifiedProperty);
            }
        });
        joined = join(initial.links, modified.links);
        angular.forEach(joined, function(name) {
            var initialLink = findByName(initial.links, name);
            var modifiedLink = findByName(modified.links, name);
            if (initialLink && modifiedLink) {
                changeSummary.links.modified.push(modifiedLink);
            } else if (initialLink) {
                changeSummary.links.deleted.push(initialLink);
            } else if (modifiedLink) {
                changeSummary.links.added.push(modifiedLink);
            }
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

}]);
