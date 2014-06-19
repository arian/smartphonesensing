
initialization = csvread('initialization.csv');
measureDistance = csvread('measureDistance.csv');
measureHeading = csvread('measureHeading.csv');

close all
hold on

%axis equal

%scatter3(initialization(:,1), initialization(:,2), initialization(:,4), 'b')
scatter3(measureDistance(:,1), measureDistance(:,2), measureDistance(:,4), 'xr')
scatter3(measureHeading(:,1), measureHeading(:,2), measureHeading(:,4), '+g')

leg = legend('after distance measurement', 'after heading measurement');
set(leg,'FontSize',18);

xlabel('x');
ylabel('y');
zlabel('weight');

view(3);