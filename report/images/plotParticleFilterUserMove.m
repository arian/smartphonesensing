
initialization = csvread('moveInitialization.csv');
userMove = csvread('userMove.csv');
userRotate = csvread('userRotate.csv');

close all
hold on

axis equal

scatter(initialization(:,1), initialization(:,2), 'b')
scatter(userMove(:,1), userMove(:,2), 'xr')
scatter(userRotate(:,1), userRotate(:,2), '+g')

legend('initial distribution', ...
    'after move(30)', 'after rotate(pi/2)');

xlabel('x');
ylabel('y');