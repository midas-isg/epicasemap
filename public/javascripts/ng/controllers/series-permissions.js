app.controller('SeriesPermission', function($scope, $rootScope, api) {
	app.makePermissionController($scope, $rootScope, api, 'Series', 'series');
});

app.controller('SeriesPermissions', function($scope, $rootScope, api) {
	app.makePermissionsController($scope, $rootScope, api, 'Series', 'series', 'Series');
});
