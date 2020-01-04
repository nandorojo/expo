#pragma once

#include <jsi/jsi.h>

namespace jsi = facebook::jsi;

enum VMType {
  JSC,
  Hermes,
};

class TypedArray {
public:
  enum Type {
    Int8Array,
    Int16Array,
    Int32Array,
    Uint8Array,
    Uint8ClampedArray,
    Uint16Array,
    Uint32Array,
    Float32Array,
    Float64Array,
    ArrayBuffer,
    None,
  };

private:
  // associate type of an array with type of a content
  template<enum Type> struct typeMap;

  template<> struct typeMap<Int8Array> { typedef int8_t type; };
  template<> struct typeMap<Int16Array> { typedef int16_t type; };
  template<> struct typeMap<Int32Array> { typedef int32_t type; };
  template<> struct typeMap<Uint8Array> { typedef uint8_t type; };
  template<> struct typeMap<Uint8ClampedArray> { typedef uint8_t type; };
  template<> struct typeMap<Uint16Array> { typedef uint16_t type; };
  template<> struct typeMap<Uint32Array> { typedef uint32_t type; };
  template<> struct typeMap<Float32Array> { typedef float type; };
  template<> struct typeMap<Float64Array> { typedef double type; };
  template<> struct typeMap<ArrayBuffer> { typedef uint8_t type; };
  template<> struct typeMap<None> { typedef uint8_t type; };

public:
  virtual ~TypedArray() {};

  template<Type T>
  using ContentType = typename typeMap<T>::type;

  static std::unique_ptr<TypedArray>& get();
  
  template <Type T>
  jsi::Value create(jsi::Runtime& runtime, std::vector<ContentType<T>> data);

  void updateWithData(jsi::Runtime& runtime, const jsi::Value& val, std::vector<uint8_t> data);

  template <Type T>
  std::vector<ContentType<T>> fromJSValue(jsi::Runtime& runtime, const jsi::Value& val);

  std::vector<uint8_t> rawFromJSValue(jsi::Runtime& runtime, const jsi::Value& val);

  Type typeFromJSValue(jsi::Runtime& runtime, const jsi::Value& val);

protected:
  virtual VMType vmName() = 0;
};

void prepareTypedArrayApi(jsi::Runtime& runtime);
