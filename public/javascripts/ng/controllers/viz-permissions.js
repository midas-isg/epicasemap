app.controller('VizPermission', function($scope, $rootScope, api) {
	app.makePermissionController($scope, $rootScope, api, 'Viz', 'vizs');
});

app.controller('VizPermissions', function($scope, $rootScope, api) {
	app.makePermissionsController($scope, $rootScope, api, 'Viz', 'vizs', 'Visualization');
});
