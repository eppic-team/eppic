CREATE TABLE benchmark(
  method varchar(10),
  bioS float,
  xtalS float,
  ca float,
  tot int,
  chk int,
  tp int,
  fn int,
  gray int,
  fail int,
  acc float,
  rec float
);

SELECT * 
 FROM benchmark 
 WHERE CA!=-1 AND method LIKE '%entr%'
 ORDER BY acc DESC, bioS ASC;

SELECT * 
 FROM benchmark 
 WHERE CA!=-1 AND method LIKE '%kaks%'
 ORDER BY acc DESC, bioS ASC;


SELECT * 
 FROM benchmark 
 WHERE method="entrNW" AND CA!=-1
 ORDER BY acc DESC;

SELECT * 
 FROM benchmark 
 WHERE method="entrW" AND CA!=-1
 ORDER BY acc DESC;

SELECT * 
 FROM benchmark 
 WHERE method="kaksNW" AND CA!=-1
 ORDER BY acc DESC;

SELECT * 
 FROM benchmark 
 WHERE method="kaksW" AND CA!=-1
 ORDER BY acc DESC;


echo "SELECT * FROM benchmark WHERE CA!=-1 AND method LIKE '%kaks%' ORDER BY acc DESC, bioS ASC LIMIT 50;" | mysql crk_benchmarking -B > kaks_fix_ca.best
echo "SELECT * FROM benchmark WHERE CA!=-1 AND method LIKE '%ent%' ORDER BY acc DESC, bioS ASC LIMIT 50;" | mysql crk_benchmarking -B > ent_fix_ca.best

echo "SELECT * FROM benchmark WHERE CA=-1 AND method LIKE '%kaks%' ORDER BY acc DESC, bioS ASC LIMIT 50;" | mysql crk_benchmarking -B > kaks_zoomed_ca.best
echo "SELECT * FROM benchmark WHERE CA=-1 AND method LIKE '%ent%' ORDER BY acc DESC, bioS ASC LIMIT 50;" | mysql crk_benchmarking -B > ent_zoomed_ca.best



