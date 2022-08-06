with import <nixpkgs> {};

let
  name = "vega";
in stdenv.mkDerivation {
  inherit name;

  src = ./target;
  nativeBuildInputs = [ makeWrapper jre ];

  installPhase = ''
    mkdir -p $out/bin
    mkdir -p $out/share/java
    cp ${name}.jar $out/share/java/${name}.jar
    makeWrapper ${jre}/bin/java $out/bin/${name} \
      --add-flags "-jar $out/share/java/${name}.jar"
  '';

}
