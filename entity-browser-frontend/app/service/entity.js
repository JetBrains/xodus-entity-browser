angular.module('xodus').factory('entitiesService', [
    '$http',
    '$q',
    '$location',
    function ($http, $q, $location) {

        function newType(object) {
            return angular.extend({}, {
                displayName: null,
                clazz: null,
                readonly: false
            }, object);
        }

        var integerPattern = /^[-+]{0,1}[0-9]*$/;
        var decimalPattern = /^[-+]?[0-9]+[.]{0,1}([0-9]+)?([eE][-+]?[0-9]+)?$/;

        var propertyTypes = [
            newType({
                displayName: 'String',
                clazz: 'java.lang.String',
                validation: {
                    pattern: null
                }
            }), newType({
                displayName: 'Boolean',
                clazz: 'java.lang.Boolean',
                validation: {
                    pattern: null
                }
            }), newType({
                displayName: 'Byte',
                clazz: 'java.lang.Byte',
                validation: {
                    pattern: integerPattern,
                    minValue: -128,
                    maxValue: 127
                }
            }), newType({
                displayName: 'Short',
                clazz: 'java.lang.Short',
                validation: {
                    pattern: integerPattern,
                    minValue: -32768,
                    maxValue: 32767
                }
            }), newType({
                displayName: 'Integer',
                clazz: 'java.lang.Integer',
                validation: {
                    pattern: integerPattern,
                    minValue: -2147483648,
                    maxValue: 2147483647
                }
            }), newType({
                displayName: 'Long',
                clazz: 'java.lang.Long',
                validation: {
                    pattern: integerPattern
                }
            }), newType({
                displayName: 'Double',
                clazz: 'java.lang.Double',
                validation: {
                    pattern: decimalPattern
                }
            }), newType({
                displayName: 'Float',
                clazz: 'java.lang.Float',
                validation: {
                    pattern: decimalPattern
                }
            })
        ];

        return function (fullDB) {
            function allPropertyTypes() {
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

            function appendValidation(property) {
                var result = propertyTypes.filter(function (p) {
                    return p.clazz === property.type.clazz;
                });
                if (result[0]) {
                    property.type.validation = angular.copy(result[0].validation);
                }
            }

            function getChanges(propertiesBefore, propertiesAfter, linksChanges) {
                return {
                    properties: getPropertiesChanges(propertiesBefore, propertiesAfter),
                    links: linksChanges,
                    blobs: []
                };
            }

            function save(entity, changeSummary) {
                var isNew = true;
                if (entity.id) {
                    isNew = false;
                }
                var path = 'api/dbs/' + fullDB.uuid + '/entities';
                if (!isNew) {
                    path = path + '/' + entity.id
                }
                if (isNew) {
                    return $http.post(path, changeSummary, {
                        params: {
                            typeId: entity.typeId
                        }
                    });
                }
                return $http.put(path, changeSummary);

            }

            function findByKey(array, key, getKeyFn) {
                var result = array.filter(function (item) {
                    return getKeyFn(item) === key;
                });
                return result.length ? result[0] : null;
            }

            function join(namedArray1, namedArray2, getKeyFn) {
                var joined = namedArray1.concat(namedArray2);
                var uniqueNames = {};
                angular.forEach(joined, function (item) {
                    if (!uniqueNames[getKeyFn(item)]) {
                        uniqueNames[getKeyFn(item)] = null;
                    }
                });
                return {
                    joined: joined,
                    uniqueNames: Object.keys(uniqueNames)
                };
            }

            function getPropertiesChanges(propertiesBefore, propertiesAfter) {
                function getPropertyItemKey(item) {
                    return item.name;
                }

                var result = [];
                var joined = join(propertiesBefore, propertiesAfter, getPropertyItemKey);
                angular.forEach(joined.uniqueNames, function (name) {
                    var initialProperty = findByKey(propertiesBefore, name, getPropertyItemKey);
                    var currentProperty = findByKey(propertiesAfter, name, getPropertyItemKey);
                    if (initialProperty && currentProperty) {
                        if (initialProperty.value !== currentProperty.value || initialProperty.type.clazz !== currentProperty.type.clazz) {
                            result.push({
                                name: currentProperty.name,
                                newValue: currentProperty
                            });
                        }
                    } else if (initialProperty) {
                        result.push({
                            name: initialProperty.name,
                            newValue: null
                        });
                    } else if (currentProperty) {
                        result.push({
                            name: currentProperty.name,
                            newValue: currentProperty
                        });
                    }
                });
                return result;
            }

            function byId(typeId, entityId) {
                if (!entityId) {
                    return $q.when(newEntity(typeId));
                }
                return $http.get('api/dbs/' + fullDB.uuid + '/entities/' + entityId).then(function (response) {
                    return response.data;
                }, function () {
                    $location.path('/error');
                });
            }

            function linkedEntities(entityId, linkName, top, skip) {
                return $http.get('api/dbs/' + fullDB.uuid + '/entities/' + entityId + '/links/' + linkName, {
                    params: {pageSize: top, offset: skip}
                }).then(function (response) {
                    return response.data;
                }, function () {
                    $location.path('/error');
                });
            }

            function deleteEntity(entityId) {
                return $http['delete']('api/dbs/' + fullDB.uuid + '/entities/' + entityId);
            }

            return {
                allPropertyTypes: allPropertyTypes,
                newProperty: newProperty,
                fromProperty: fromProperty,
                appendValidation: appendValidation,
                newEntity: newEntity,
                getChanges: getChanges,
                save: save,
                byId: byId,
                linkedEntities: linkedEntities,
                deleteEntity: deleteEntity
            }
        };

    }]
);
