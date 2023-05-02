const express = require('express');
const app = express();
const path = require('path');
const router = express.Router();

router.get('/', function (req, res) {
  res.sendFile(path.join(__dirname + '/home.html'));
  //__dirname : It will resolve to your project folder.
});

/* router.get('/about',function(req,res){
  res.sendFile(path.join(__dirname+'/about.html'));
});

router.get('/sitemap',function(req,res){
  res.sendFile(path.join(__dirname+'/sitemap.html'));
}); */

//add the router
app.use('/', router);
//Resources
app.use(express.static(__dirname + '/images'));
app.use(express.static(__dirname + '/chart'));
// All the Results
app.use(express.static(path.join(__dirname + '/results')));
app.listen(process.env.port || 3000);

console.log('Running at Port 3000');