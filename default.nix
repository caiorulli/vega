with import <nixpkgs> {};

let
  name = "vega-1.0.0";
in stdenv.mkDerivation {
  inherit name;

  src = ./src;
  nativeBuildInputs = [ clojure makeWrapper jre ];

  buildPhase = ''
    clj -T:build uber
  '';

  installPhase = ''
    mkdir -p $out/bin
    mkdir -p $out/share/java
    cp target/vega-1.0.0-standalone.jar $out/share/java/${name}.jar
    makeWrapper ${jre}/bin/java $out/bin/${name} \
      --add-flags "-jar $out/share/java/${name}.jar"
  '';

}
