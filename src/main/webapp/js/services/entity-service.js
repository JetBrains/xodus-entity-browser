angular.module('xodus').service('EntitiesService', [function() {
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

    function allTypes() {
        return angular.copy(propertyTypes);
    }

    function newProperty() {
        var copy = angular.copy(propertyTypes[0]);
        copy.name = null;
        copy.value = null;
        return copy;
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

}]);
